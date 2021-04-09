package ua.gov.mdtu.ddm.lowcode.bpms.delegate.connector;

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
import org.camunda.spin.Spin;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ua.gov.mdtu.ddm.general.integration.ceph.service.FormDataCephService;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.enums.PlatformHttpHeader;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.dto.DataFactoryConnectorResponse;

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
  private final String springAppName;

  /**
   * Method for performing requests to data factory
   *
   * @param requestEntity {@link RequestEntity} entity
   * @return response from data factory
   */
  protected DataFactoryConnectorResponse perform(RequestEntity<?> requestEntity) {
    logRequest(requestEntity);
    var httpResponse = restTemplate.exchange(requestEntity, String.class);
    logResponse(httpResponse);

    var spin = Objects.isNull(httpResponse.getBody()) ? null : Spin.JSON(httpResponse.getBody());
    return DataFactoryConnectorResponse.builder()
        .statusCode(httpResponse.getStatusCode().value())
        .responseBody(spin)
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

  private void logRequest(RequestEntity<?> request) {
    if (log.isDebugEnabled()) {
      log.debug("Sending {} request to {} with request payload - {} and headers - {}",
          request.getMethod(), request.getUrl(), request.getBody(), request.getHeaders());
      return;
    }
    log.info("Sending {} request to {}", request.getMethod(), request.getUrl());
  }

  private void logResponse(ResponseEntity<?> response) {
    if (log.isDebugEnabled()) {
      log.debug("Received {} status code with response body - {}", response.getStatusCode(),
          response.getBody());
      return;
    }
    log.info("Received {} status code", response.getStatusCode());
  }
}
