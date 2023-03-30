/*
 * Copyright 2023 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.bpms.extension.delegate.connector.rest;

import com.epam.digital.data.platform.bpms.extension.config.properties.ExternalSystemConfigurationProperties;
import com.epam.digital.data.platform.bpms.extension.config.properties.ExternalSystemConfigurationProperties.AuthenticationConfiguration;
import com.epam.digital.data.platform.bpms.extension.config.properties.ExternalSystemConfigurationProperties.AuthenticationConfiguration.AuthenticationType;
import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.dto.RegistryConnectorResponse;
import com.epam.digital.data.platform.bpms.extension.exception.AuthConfigurationException;
import com.epam.digital.data.platform.bpms.extension.service.TokenCacheService;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is used for connecting to
 * external-systems
 */
@Slf4j
@Component(ExternalSystemConnectorDelegate.DELEGATE_NAME)
public class ExternalSystemConnectorDelegate extends BaseRestTemplateConnectorDelegate {

  public static final String DELEGATE_NAME = "externalSystemConnectorDelegate";

  private final Map<String, ExternalSystemConfigurationProperties> externalSystemsConfiguration;
  private final TokenCacheService tokenCacheService;

  @SystemVariable(name = "systemName")
  private NamedVariableAccessor<String> systemNameVariable;
  @SystemVariable(name = "methodName")
  private NamedVariableAccessor<String> methodNameVariable;
  @SystemVariable(name = "operationName")
  private NamedVariableAccessor<String> operationNameVariable;
  @SystemVariable(name = "requestParameters")
  private NamedVariableAccessor<Map<String, String>> requestParametersVariable;
  @SystemVariable(name = "requestHeaders")
  private NamedVariableAccessor<Map<String, String>> requestHeadersVariable;
  @SystemVariable(name = "payload")
  private NamedVariableAccessor<Object> payloadVariable;
  @SystemVariable(name = "response", isTransient = true)
  private NamedVariableAccessor<RegistryConnectorResponse> responseVariable;

  public ExternalSystemConnectorDelegate(RestTemplate restTemplate,
      Map<String, ExternalSystemConfigurationProperties> externalSystemsConfiguration,
      TokenCacheService tokenCacheService) {
    super(restTemplate);
    this.externalSystemsConfiguration = externalSystemsConfiguration;
    this.tokenCacheService = tokenCacheService;
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Override
  protected void executeInternal(DelegateExecution execution) throws Exception {
    var externalSystemName = systemNameVariable.from(execution).getOrThrow();
    var operationName = defineOperationName(execution);

    var externalSystemConfiguration = getExternalSystemConfiguration(externalSystemName);
    var operationConfiguration = getOperationConfiguration(externalSystemName, operationName);
    var auth = externalSystemConfiguration.getAuth();
    checkAuthenticationConfig(auth, externalSystemName);

    var requestParameters =
        toMultiValueMapSeparatedByComma(requestParametersVariable.from(execution).get());
    var requestHeaders = new HttpHeaders(toMultiValueMapSeparatedByComma(
        requestHeadersVariable.from(execution).get()));
    var payload = payloadVariable.from(execution).getOptional().map(Object::toString).orElse(null);

    var externalSystemUrl = externalSystemConfiguration.getUrl();
    authenticate(requestHeaders, auth, externalSystemUrl);

    var uri = buildUri(externalSystemUrl, operationConfiguration.getResourcePath(),
        requestParameters);
    var method = operationConfiguration.getMethod();
    var httpEntity = new HttpEntity<>(payload, requestHeaders);

    var responseValue = sendRequest(uri, method, httpEntity);

    responseVariable.on(execution).set(responseValue);
  }

  private void checkAuthenticationConfig(AuthenticationConfiguration auth, String extSysName) {
    var isAuthConfigDefined = Objects.nonNull(auth);

    if (isAuthConfigDefined && !AuthenticationType.NO_AUTH.equals(auth.getType())) {
      isAuthConfigDefined = isAuthSecretDefined(auth);
    }

    if (!isAuthConfigDefined) {
      throw new AuthConfigurationException(String.format(
          "Authentication configuration for external-system with name %s not configured",
          extSysName));
    }
  }

  private boolean isAuthSecretDefined(AuthenticationConfiguration auth) {
    var secret = auth.getSecret();
    if (Objects.isNull(secret)) {
      return false;
    } else if (AuthenticationType.BASIC.equals(auth.getType())) {
      return StringUtils.isNotBlank(secret.getUsername()) && StringUtils.isNotBlank(
          secret.getPassword());
    } else {
      return StringUtils.isNotBlank(secret.getToken());
    }
  }

  private ExternalSystemConfigurationProperties getExternalSystemConfiguration(
      String externalSystemName) {
    var configuration = externalSystemsConfiguration.get(externalSystemName);
    if (Objects.isNull(configuration)) {
      throw new IllegalArgumentException(
          String.format("External-system with name %s not configured", externalSystemName));
    }
    return configuration;
  }

  private ExternalSystemConfigurationProperties.OperationConfiguration getOperationConfiguration(
      String externalSystemName, String operationName) {
    var externalSystemConfiguration = getExternalSystemConfiguration(externalSystemName);

    return Optional.ofNullable(externalSystemConfiguration.getOperations())
        .map(operations -> operations.get(operationName))
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("Operation %s in external-system %s not configured", operationName,
                externalSystemName)));
  }

  private void authenticate(HttpHeaders requestHeaders, AuthenticationConfiguration auth,
      String externalSystemUrl) {
    if (AuthenticationType.BASIC.equals(auth.getType())) {
      authenticateWithBasic(requestHeaders, auth.getSecret());
    } else if (AuthenticationType.AUTH_TOKEN_BEARER.equals(auth.getType())) {
      authenticateWithPartnerToken(requestHeaders, auth, externalSystemUrl);
    } else if (AuthenticationType.BEARER.equals(auth.getType())
        || AuthenticationType.AUTH_TOKEN.equals(auth.getType())) {
      var authToken = auth.getSecret().getToken();
      requestHeaders.setBearerAuth(authToken);
    }
  }

  private void authenticateWithBasic(HttpHeaders requestHeaders,
      AuthenticationConfiguration.Secret secret) {
    var username = secret.getUsername();
    var password = secret.getPassword();
    requestHeaders.setBasicAuth(username, password);
  }

  private void authenticateWithPartnerToken(HttpHeaders requestHeaders,
      AuthenticationConfiguration auth, String externalSystemUrl) {
    var cacheName = AuthenticationType.AUTH_TOKEN_BEARER.getCode();
    var authToken = tokenCacheService.getCachedTokenOrElse(cacheName, externalSystemUrl,
        () -> getTokenFromExternalSystem(auth, externalSystemUrl));

    requestHeaders.setBearerAuth(Objects.requireNonNull(authToken));
  }

  private String getTokenFromExternalSystem(AuthenticationConfiguration auth,
      String externalSystemUrl) {
    var partnerToken = auth.getSecret().getToken();
    URI concatenatedUrl;
    var authUrl = auth.getAuthUrl();

    if (authUrl.startsWith("/")) {
      concatenatedUrl = UriComponentsBuilder.fromUriString(externalSystemUrl)
          .pathSegment(authUrl.split("/"))
          .pathSegment(partnerToken)
          .build().toUri();
    } else {
      concatenatedUrl = UriComponentsBuilder.fromHttpUrl(authUrl)
          .pathSegment(partnerToken)
          .build().toUri();
    }

    var response = sendRequest(concatenatedUrl, HttpMethod.GET, null);
    return response.getResponseBody().jsonPath(auth.getAccessTokenJsonPath()).stringValue();
  }

  private String defineOperationName(DelegateExecution execution) {
    var operationName = operationNameVariable.from(execution).get();
    var methodName = methodNameVariable.from(execution).get();

    if (Objects.isNull(operationName) && Objects.isNull(methodName)) {
      throw new IllegalArgumentException(
          "Variable not found. One of 'operationName' or 'methodName' must be specified.");
    } else {
      return Objects.nonNull(methodName) ? methodName : operationName;
    }
  }
}