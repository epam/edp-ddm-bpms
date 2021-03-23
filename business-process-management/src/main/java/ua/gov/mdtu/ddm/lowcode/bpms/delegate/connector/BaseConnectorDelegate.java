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
import ua.gov.mdtu.ddm.general.integration.ceph.service.FormDataCephService;
import ua.gov.mdtu.ddm.general.localization.MessageResolver;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.enums.PlatformHttpHeader;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.dto.DataFactoryConnectorResponse;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.dto.enums.DataFactoryError;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to provide common
 * logic for working with the data factory for all delegates
 */
@RequiredArgsConstructor
@Slf4j
public abstract class BaseConnectorDelegate implements JavaDelegate {

  protected static final String RESOURCE_VARIABLE = "resource";
  protected static final String RESOURCE_ID_VARIABLE = "id";
  protected static final String PAYLOAD_VARIABLE = "payload";
  protected static final String RESPONSE_VARIABLE = "response";

  private final RestTemplate restTemplate;
  private final FormDataCephService formDataCephService;
  private final ObjectMapper objectMapper;
  private final MessageResolver messageResolver;
  private final String springAppName;

  /**
   * Method for performing requests to data factory
   *
   * @param requestEntity {@link RequestEntity} entity
   * @return response from data factory
   */
  protected DataFactoryConnectorResponse perform(RequestEntity<?> requestEntity) {
    var httpResponse = restTemplate.exchange(requestEntity, String.class);

    logSuccessfulRequest(requestEntity, httpResponse);

    return DataFactoryConnectorResponse.builder()
        .statusCode(httpResponse.getStatusCode().value())
        .responseBody(httpResponse.getBody())
        .headers(httpResponse.getHeaders())
        .build();
  }

  /**
   * Method for getting http headers from {@link DelegateExecution} object. Additionally sets the
   * system http headers.
   *
   * @param delegateExecution {@link DelegateExecution} object
   * @return list of http headers
   */
  @SuppressWarnings("unchecked")
  protected HttpHeaders getHeaders(DelegateExecution delegateExecution) {
    var headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add(PlatformHttpHeader.X_SOURCE_SYSTEM.getName(), "Low-code Platform");
    headers.add(PlatformHttpHeader.X_SOURCE_APPLICATION.getName(), springAppName);
    headers.add(PlatformHttpHeader.X_SOURCE_BUSINESS_PROCESS.getName(),
        ((ExecutionEntity) delegateExecution).getProcessDefinition().getName());
    headers.add(PlatformHttpHeader.X_SOURCE_BUSINESS_ACTIVITY.getName(),
        delegateExecution.getActivityInstanceId());

    getAccessToken(delegateExecution).ifPresent(xAccessToken ->
        headers.add(PlatformHttpHeader.X_ACCESS_TOKEN.getName(), xAccessToken));
    var xDigitalSignatureCephKey = (String) delegateExecution
        .getVariable("x_digital_signature_ceph_key");
    if (!StringUtils.isBlank(xDigitalSignatureCephKey)) {
      headers.add(PlatformHttpHeader.X_DIGITAL_SIGNATURE.getName(), xDigitalSignatureCephKey);
    }
    var xDigitalSignatureDerivedCephKey = (String) delegateExecution
        .getVariable("x_digital_signature_derived_ceph_key");
    if (!StringUtils.isBlank(xDigitalSignatureDerivedCephKey)) {
      headers.add(PlatformHttpHeader.X_DIGITAL_SIGNATURE_DERIVED.getName(),
          xDigitalSignatureDerivedCephKey);
    }

    var customHeaders = (Map<String, String>) delegateExecution.getVariable("headers");
    if (customHeaders != null) {
      customHeaders.entrySet().stream().filter(entry -> !headers.containsKey(entry.getKey()))
          .forEach(entry -> headers.add(entry.getKey(), entry.getValue()));
    }

    return headers;
  }

  /**
   * Method for getting an access token from {@link DelegateExecution} object.
   *
   * @param delegateExecution {@link DelegateExecution} object
   * @return access token body
   */
  @SneakyThrows
  protected Optional<String> getAccessToken(DelegateExecution delegateExecution) {
    var xAccessTokenCephKey = (String) delegateExecution.getVariable("x_access_token_ceph_key");
    if (StringUtils.isBlank(xAccessTokenCephKey)) {
      return Optional.empty();
    }
    var xAccessTokenCephFromData = formDataCephService.getFormData(xAccessTokenCephKey);

    return Optional.ofNullable(xAccessTokenCephFromData.getAccessToken());
  }

  /**
   * Method for building an error that occurs when reading data from a data factory.
   *
   * @param requestEntity {@link RequestEntity} entity
   * @param ex {@link RestClientResponseException} exception
   * @return a runtime exception
   */
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

  /**
   * Method for building an error that occurs when updating data in a data factory.
   *
   * @param requestEntity {@link RequestEntity} entity
   * @param ex {@link RestClientResponseException} exception
   * @return a runtime exception
   */
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
