package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.api.dto.enums.PlatformHttpHeader;
import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.dto.ConnectorResponse;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to provide common
 * logic for working with the data factory for all delegates
 */
@RequiredArgsConstructor
public abstract class BaseConnectorDelegate extends BaseJavaDelegate {

  protected static final String RESOURCE_SETTINGS = "settings";
  protected static final String RESOURCE_EXCERPTS = "excerpts";

  private final RestTemplate restTemplate;
  private final String springAppName;

  @SystemVariable(name = "x_access_token")
  private NamedVariableAccessor<String> xAccessTokenVariable;
  @SystemVariable(name = "x_digital_signature_ceph_key")
  private NamedVariableAccessor<String> xDigitalSignatureCephKeyVariable;
  @SystemVariable(name = "x_digital_signature_derived_ceph_key")
  protected NamedVariableAccessor<String> xDigitalSignatureDerivedCephKeyVariable;
  @SystemVariable(name = "headers")
  private NamedVariableAccessor<Map<String, String>> headersVariable;

  @SystemVariable(name = "resource")
  protected NamedVariableAccessor<String> resourceVariable;
  @SystemVariable(name = "id")
  protected NamedVariableAccessor<String> resourceIdVariable;
  @SystemVariable(name = "payload", isTransient = true)
  protected NamedVariableAccessor<SpinJsonNode> payloadVariable;
  @SystemVariable(name = "response", isTransient = true)
  protected NamedVariableAccessor<ConnectorResponse> responseVariable;

  /**
   * Method for performing requests to data factory
   *
   * @param requestEntity {@link RequestEntity} entity
   * @return response from data factory
   */
  protected ConnectorResponse perform(RequestEntity<?> requestEntity) {
    var httpResponse = restTemplate.exchange(requestEntity, String.class);

    var spin = Objects.isNull(httpResponse.getBody()) ? null : Spin.JSON(httpResponse.getBody());
    return ConnectorResponse.builder()
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
  protected HttpHeaders getHeaders(DelegateExecution delegateExecution) {
    var headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add(PlatformHttpHeader.X_SOURCE_SYSTEM.getName(), "Low-code Platform");
    headers.add(PlatformHttpHeader.X_SOURCE_APPLICATION.getName(), springAppName);
    headers.add(PlatformHttpHeader.X_SOURCE_BUSINESS_PROCESS.getName(),
        ((ExecutionEntity) delegateExecution).getProcessDefinition().getKey());
    headers.add(PlatformHttpHeader.X_SOURCE_BUSINESS_ACTIVITY.getName(),
        delegateExecution.getCurrentActivityId());
    headers.add(PlatformHttpHeader.X_SOURCE_BUSINESS_ACTIVITY_INSTANCE_ID.getName(),
        delegateExecution.getActivityInstanceId());
    headers.add(PlatformHttpHeader.X_SOURCE_BUSINESS_PROCESS_INSTANCE_ID.getName(),
        delegateExecution.getProcessInstanceId());
    headers.add(PlatformHttpHeader.X_SOURCE_BUSINESS_PROCESS_DEFINITION_ID.getName(),
        delegateExecution.getProcessDefinitionId());

    getAccessToken(delegateExecution).ifPresent(xAccessToken ->
        headers.add(PlatformHttpHeader.X_ACCESS_TOKEN.getName(), xAccessToken));
    var xDigitalSignatureCephKey = xDigitalSignatureCephKeyVariable.from(delegateExecution).get();
    headers.add(PlatformHttpHeader.X_DIGITAL_SIGNATURE.getName(), xDigitalSignatureCephKey);
    var xDigitalSignatureDerivedCephKey = xDigitalSignatureDerivedCephKeyVariable
        .from(delegateExecution).get();
    if (!StringUtils.isBlank(xDigitalSignatureDerivedCephKey)) {
      headers.add(PlatformHttpHeader.X_DIGITAL_SIGNATURE_DERIVED.getName(),
          xDigitalSignatureDerivedCephKey);
    }

    headersVariable.from(delegateExecution).getOptional()
        .map(Map::entrySet).stream().flatMap(Collection::stream)
        .filter(entry -> !headers.containsKey(entry.getKey()))
        .forEach(entry -> headers.add(entry.getKey(), entry.getValue()));

    return headers;
  }

  /**
   * Method for getting an access token from {@link DelegateExecution} object.
   *
   * @param delegateExecution {@link DelegateExecution} object
   * @return access token body
   */
  protected Optional<String> getAccessToken(DelegateExecution delegateExecution) {
    return xAccessTokenVariable.from(delegateExecution).getOptional();
  }
}