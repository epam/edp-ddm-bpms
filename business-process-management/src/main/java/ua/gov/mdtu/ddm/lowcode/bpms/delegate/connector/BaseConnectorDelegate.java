package ua.gov.mdtu.ddm.lowcode.bpms.delegate.connector;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDetailsDto;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.UserDataValidationErrorDto;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ValidationErrorDto;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.dto.DataFactoryConnectorResponse;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.dto.enums.DataFactoryError;
import ua.gov.mdtu.ddm.lowcode.bpms.service.MessageResolver;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.CamundaSystemException;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.UserDataValidationException;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseConnectorDelegate implements JavaDelegate {

  protected static final String RESOURCE_VARIABLE = "resource";
  protected static final String RESOURCE_ID_VARIABLE = "id";
  protected static final String PAYLOAD_VARIABLE = "payload";
  protected static final String RESPONSE_VARIABLE = "response";

  private final RestTemplate restTemplate;
  private final CephService cephService;
  private final JacksonJsonParser jacksonJsonParser;
  private final MessageResolver messageResolver;
  private final String springAppName;
  private final String cephBucketName;

  protected DataFactoryConnectorResponse perform(RequestEntity<?> requestEntity) {
    var httpResponse = restTemplate.exchange(requestEntity, String.class);

    log.info("Successfully sent {} request to {}", requestEntity.getMethod(),
        requestEntity.getUrl());

    return DataFactoryConnectorResponse.builder()
        .statusCode(httpResponse.getStatusCode().value())
        .responseBody(httpResponse.getBody())
        .headers(httpResponse.getHeaders())
        .build();
  }

  @SuppressWarnings("unchecked")
  protected HttpHeaders getHeaders(DelegateExecution delegateExecution) {
    var headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("X-Source-System", "Low-code Platform");
    headers.add("X-Source-Application", springAppName);
    headers.add("X-Source-Business-Process",
        ((ExecutionEntity) delegateExecution).getProcessDefinition().getName());
    headers.add("X-Source-Business-Activity",
        delegateExecution.getActivityInstanceId());

    getAccessToken(delegateExecution).ifPresent(xAccessToken ->
        headers.add("X-Access-Token", xAccessToken));
    var xDigitalSignatureCephKey = (String) delegateExecution
        .getVariable("x_digital_signature_ceph_key");
    if (!StringUtils.isBlank(xDigitalSignatureCephKey)) {
      headers.add("X-Digital-Signature", xDigitalSignatureCephKey);
    }
    var xDigitalSignatureDerivedCephKey = (String) delegateExecution
        .getVariable("x_digital_signature_derived_ceph_key");
    if (!StringUtils.isBlank(xDigitalSignatureDerivedCephKey)) {
      headers.add("X-Digital-Signature-Derived", xDigitalSignatureDerivedCephKey);
    }

    var customHeaders = (Map<String, String>) delegateExecution.getVariable("headers");
    if (customHeaders != null) {
      customHeaders.entrySet().stream().filter(entry -> !headers.containsKey(entry.getKey()))
          .forEach(entry -> headers.add(entry.getKey(), entry.getValue()));
    }

    return headers;
  }
  protected Optional<String> getAccessToken(DelegateExecution delegateExecution) {
    var xAccessTokenCephKey = (String) delegateExecution.getVariable("x_access_token_ceph_key");
    if (StringUtils.isBlank(xAccessTokenCephKey)) {
      return Optional.empty();
    }
    var xAccessTokenCephDoc = cephService.getContent(cephBucketName, xAccessTokenCephKey);

    Map<String, Object> map = jacksonJsonParser.parseMap(xAccessTokenCephDoc);
    return Optional.ofNullable(map.get("x-access-token")).map(Object::toString);
  }

  protected RuntimeException buildReadableException(RequestEntity<?> requestEntity,
      RestClientResponseException ex) {
    var httpStatus = HttpStatus.valueOf(ex.getRawStatusCode());
    var isValidationException = Objects.equals(HttpStatus.UNPROCESSABLE_ENTITY, httpStatus) ||
        Objects.equals(HttpStatus.NOT_FOUND, httpStatus);
    return buildException(requestEntity, ex, isValidationException);
  }

  protected RuntimeException buildUpdatableException(RequestEntity<?> requestEntity,
      RestClientResponseException ex) {
    var httpStatus = HttpStatus.valueOf(ex.getRawStatusCode());
    var isValidationException = Objects.equals(HttpStatus.UNPROCESSABLE_ENTITY, httpStatus);
    return buildException(requestEntity, ex, isValidationException);
  }

  private RuntimeException buildException(RequestEntity<?> requestEntity,
      RestClientResponseException ex, boolean isValidationException) {
    log.debug("Request headers : {}", requestEntity.getHeaders());
    log.debug("Request payload : {}", requestEntity.getBody());
    log.debug("Response body : {}", ex.getResponseBodyAsString());
    if (isValidationException) {
      log.info("{} request to {} returned result status {}, message - {}",
          requestEntity.getMethod(), requestEntity.getUrl(),
          ex.getRawStatusCode(), ex.getMessage(), ex);
      return validationException(ex.getResponseBodyAsString());
    }
    log.error("{} request to {} failed with status {}, message - {}", requestEntity.getMethod(),
        requestEntity.getUrl(), ex.getRawStatusCode(), ex.getMessage(), ex);
    return camundaSystemException(ex.getResponseBodyAsString());
  }

  private CamundaSystemException camundaSystemException(String responseBody) {
    var responseMap = jacksonJsonParser.parseMap(responseBody);

    var traceId = (String) responseMap.get("traceId");
    var code = (String) responseMap.get("code");

    var message = "System Error";
    var dataFactoryError = DataFactoryError.fromNameOrDefaultRuntimeError(code);
    var localizedMessage = messageResolver.getMessage(dataFactoryError.getTitleKey());
    return new CamundaSystemException(traceId, code, message, localizedMessage);
  }

  @SuppressWarnings("unchecked")
  private UserDataValidationException validationException(String responseBody) {
    var responseMap = jacksonJsonParser.parseMap(responseBody);

    var traceId = (String) responseMap.get("traceId");
    var code = (String) responseMap.get("code");

    var userDataValidationErrorDto = new UserDataValidationErrorDto();
    userDataValidationErrorDto.setTraceId(traceId);
    userDataValidationErrorDto.setCode(code);
    userDataValidationErrorDto.setMessage("Validation error");

    var details = (Map<String, Object>) responseMap.get("details");
    if (details == null) {
      return new UserDataValidationException(userDataValidationErrorDto);
    }

    var errors = (List<Object>) details.get("errors");
    if (errors == null) {
      return new UserDataValidationException(userDataValidationErrorDto);
    }

    var errorDetailDto = new ErrorDetailsDto();
    var validationErrorDtos = errors.stream().map(error -> {
      var errorMap = (Map<String, Object>) error;

      var message = (String) errorMap.get("message");
      var field = (String) errorMap.get("field");
      var value = (String) errorMap.get("value");

      var validationErrorDto = new ValidationErrorDto();
      validationErrorDto.setMessage(message);
      validationErrorDto.setField(field);
      validationErrorDto.setValue(value);
      return validationErrorDto;
    }).collect(Collectors.toList());
    errorDetailDto.setErrors(validationErrorDtos);
    userDataValidationErrorDto.setDetails(errorDetailDto);
    return new UserDataValidationException(userDataValidationErrorDto);
  }
}
