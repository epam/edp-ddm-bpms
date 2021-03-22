package ua.gov.mdtu.ddm.lowcode.bpms.delegate.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import ua.gov.mdtu.ddm.general.errorhandling.dto.ErrorDetailDto;
import ua.gov.mdtu.ddm.general.errorhandling.dto.ErrorsListDto;
import ua.gov.mdtu.ddm.general.errorhandling.dto.SystemErrorDto;
import ua.gov.mdtu.ddm.general.errorhandling.dto.ValidationErrorDto;
import ua.gov.mdtu.ddm.general.errorhandling.exception.SystemException;
import ua.gov.mdtu.ddm.general.errorhandling.exception.ValidationException;
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.dto.DataFactoryConnectorResponse;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.dto.enums.DataFactoryError;
import ua.gov.mdtu.ddm.lowcode.bpms.service.MessageResolver;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseConnectorDelegate implements JavaDelegate {

  protected static final String RESOURCE_VARIABLE = "resource";
  protected static final String RESOURCE_ID_VARIABLE = "id";
  protected static final String PAYLOAD_VARIABLE = "payload";
  protected static final String RESPONSE_VARIABLE = "response";

  private final RestTemplate restTemplate;
  private final CephService cephService;
  private final ObjectMapper objectMapper;
  private final MessageResolver messageResolver;
  private final String springAppName;
  private final String cephBucketName;

  protected DataFactoryConnectorResponse perform(RequestEntity<?> requestEntity) {
    var httpResponse = restTemplate.exchange(requestEntity, String.class);

    logSuccessfulRequest(requestEntity, httpResponse);

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

  @SneakyThrows
  protected Optional<String> getAccessToken(DelegateExecution delegateExecution) {
    var xAccessTokenCephKey = (String) delegateExecution.getVariable("x_access_token_ceph_key");
    if (StringUtils.isBlank(xAccessTokenCephKey)) {
      return Optional.empty();
    }
    var xAccessTokenCephDoc = cephService.getContent(cephBucketName, xAccessTokenCephKey);

    Map<String, Object> map = objectMapper.readerForMapOf(Object.class)
        .readValue(xAccessTokenCephDoc);
    return Optional.ofNullable(map.get("x-access-token")).map(Object::toString);
  }

  protected RuntimeException buildReadableException(RequestEntity<?> requestEntity,
      RestClientResponseException ex) {
    var httpStatus = HttpStatus.valueOf(ex.getRawStatusCode());
    var isValidationException = Objects.equals(HttpStatus.UNPROCESSABLE_ENTITY, httpStatus) ||
        Objects.equals(HttpStatus.NOT_FOUND, httpStatus);

    var exception = buildException(requestEntity, ex, isValidationException);

    if (Objects.equals(HttpStatus.NOT_FOUND, httpStatus)) {
      var localizedMessage = messageResolver
          .getMessage(DataFactoryError.NOT_FOUND.getTitleKey());

      ((ValidationException) exception).getDetails()
          .setErrors(Collections.singletonList(new ErrorDetailDto(localizedMessage, null, null)));
    }

    return exception;
  }

  protected RuntimeException buildUpdatableException(RequestEntity<?> requestEntity,
      RestClientResponseException ex) {
    var httpStatus = HttpStatus.valueOf(ex.getRawStatusCode());
    var isValidationException = Objects.equals(HttpStatus.UNPROCESSABLE_ENTITY, httpStatus);
    return buildException(requestEntity, ex, isValidationException);
  }

  private RuntimeException buildException(RequestEntity<?> requestEntity,
      RestClientResponseException ex, boolean isValidationException) {
    logExceptionRequest(requestEntity, ex, isValidationException);
    return isValidationException ? validationException(ex.getResponseBodyAsString())
        : camundaSystemException(ex.getResponseBodyAsString());
  }

  @SneakyThrows
  private SystemException camundaSystemException(String responseBody) {
    var systemErrorDto = objectMapper.readValue(responseBody, SystemErrorDto.class);

    var dataFactoryError = DataFactoryError.fromNameOrDefaultRuntimeError(systemErrorDto.getCode());
    var localizedMessage = messageResolver.getMessage(dataFactoryError.getTitleKey());

    systemErrorDto.setLocalizedMessage(localizedMessage);
    return new SystemException(systemErrorDto);
  }

  @SneakyThrows
  private ValidationException validationException(String responseBody) {
    var validationErrorDto = objectMapper.readValue(responseBody, ValidationErrorDto.class);

    if (Objects.nonNull(validationErrorDto.getDetails())) {
      var localizedMessage = messageResolver
          .getMessage(DataFactoryError.VALIDATION_ERROR.getTitleKey());
      validationErrorDto.getDetails().getErrors()
          .forEach(errorDetailDto -> errorDetailDto.setMessage(localizedMessage));
    } else {
      validationErrorDto.setDetails(new ErrorsListDto());
    }

    return new ValidationException(validationErrorDto);
  }

  private void logSuccessfulRequest(RequestEntity<?> request, ResponseEntity<?> response) {
    if (log.isDebugEnabled()) {
      log.debug("{} request to {} with request payload - {} and headers - {} "
              + "returned {} status code with response body - {}",
          request.getMethod(), request.getUrl(), request.getBody(), request.getHeaders(),
          response.getStatusCode(), response.getBody());
      return;
    }
    log.info("{} request to {} returned {} status code", request.getMethod(), request.getUrl(),
        response.getStatusCode());
  }

  private void logExceptionRequest(RequestEntity<?> requestEntity,
      RestClientResponseException ex, boolean isValidationException) {
    if (log.isDebugEnabled()) {
      log.debug("{} request to {} with request payload - {} and headers - {} "
              + "returned {} status code with message - {} and response body - {}",
          requestEntity.getMethod(), requestEntity.getUrl(), requestEntity.getBody(),
          requestEntity.getHeaders(), ex.getRawStatusCode(), ex.getMessage(),
          ex.getResponseBodyAsString());
      return;
    }
    if (isValidationException) {
      log.info("{} request to {} returned {} status code with message - {}",
          requestEntity.getMethod(), requestEntity.getUrl(), ex.getRawStatusCode(),
          ex.getMessage());
    } else {
      log.error("{} request to {} returned {} status code with message - {}",
          requestEntity.getMethod(), requestEntity.getUrl(), ex.getRawStatusCode(),
          ex.getMessage());
    }
  }
}
