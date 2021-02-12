package ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.connector;

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
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.dto.DataFactoryConnectorResponse;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.dto.enums.DataFactoryError;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.service.MessageResolver;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.CamundaSystemException;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.UserDataValidationException;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseDataFactoryConnectorDelegate implements JavaDelegate {

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
      customHeaders.forEach(headers::add);
    }

    return headers;
  }
  private Optional<String> getAccessToken(DelegateExecution delegateExecution) {
    var xAccessTokenCephKey = (String) delegateExecution.getVariable("x_access_token_ceph_key");
    if (StringUtils.isBlank(xAccessTokenCephKey)) {
      return Optional.empty();
    }
    var xAccessTokenCephDoc = cephService.getContent(cephBucketName, xAccessTokenCephKey);

    Map<String, Object> map = jacksonJsonParser.parseMap(xAccessTokenCephDoc);
    return Optional.ofNullable(map.get("x-access-token")).map(Object::toString);
  }

  protected RuntimeException buildReadableException(RestClientResponseException ex) {
    var httpStatus = HttpStatus.valueOf(ex.getRawStatusCode());
    if (Objects.equals(HttpStatus.UNPROCESSABLE_ENTITY, httpStatus) ||
        Objects.equals(HttpStatus.NOT_FOUND, httpStatus)) {
      log.info("Request to dataFactory returned result status {}, message - {}",
          ex.getRawStatusCode(), ex.getMessage(), ex);

      return validationException(ex.getResponseBodyAsString());
    }

    log.error("Request to dataFactory failed with status {}, message - {}",
        ex.getRawStatusCode(), ex.getMessage(), ex);
    return camundaSystemException(ex.getResponseBodyAsString());
  }

  protected RuntimeException buildUpdatableException(RestClientResponseException ex) {
    var httpStatus = HttpStatus.valueOf(ex.getRawStatusCode());
    if (Objects.equals(HttpStatus.UNPROCESSABLE_ENTITY, httpStatus)) {
      log.info("Request to dataFactory returned result status {}, message - {}",
          ex.getRawStatusCode(), ex.getMessage(), ex);

      return validationException(ex.getResponseBodyAsString());
    }

    log.error("Request to dataFactory failed with status {}, message - {}",
        ex.getRawStatusCode(), ex.getMessage(), ex);
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
    userDataValidationErrorDto.setType(code);
    userDataValidationErrorDto.setMessage("Validation error");

    var dataFactoryError = DataFactoryError.fromNameOrDefaultRuntimeError(code);
    var localizedMessage = messageResolver.getMessage(dataFactoryError.getTitleKey());
    userDataValidationErrorDto.setLocalizedMessage(localizedMessage);

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
      validationErrorDto.setContext(Map.of(field, value));
      return validationErrorDto;
    }).collect(Collectors.toList());
    errorDetailDto.setValidationErrors(validationErrorDtos);
    userDataValidationErrorDto.setDetails(errorDetailDto);
    return new UserDataValidationException(userDataValidationErrorDto);
  }
}
