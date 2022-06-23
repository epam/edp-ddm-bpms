/*
 * Copyright 2021 EPAM Systems.
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
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Value;
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

  private static final String BASIC_SECRET_USERNAME_FIELD = "username";
  private static final String BASIC_SECRET_PASSWORD_FIELD = "password";
  private static final String PARTNER_TOKEN_SECRET_TOKEN_FIELD = "token";

  private final Map<String, ExternalSystemConfigurationProperties> externalSystemsConfiguration;
  private final KubernetesClient kubernetesClient;
  private final String currentNamespace;

  @SystemVariable(name = "systemName")
  private NamedVariableAccessor<String> systemNameVariable;
  @SystemVariable(name = "methodName")
  private NamedVariableAccessor<String> methodNameVariable;
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
      KubernetesClient kubernetesClient,
      @Value("${kubernetes.namespace.current}") String currentNamespace) {
    super(restTemplate);
    this.externalSystemsConfiguration = externalSystemsConfiguration;
    this.kubernetesClient = kubernetesClient;
    this.currentNamespace = currentNamespace;
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Override
  protected void executeInternal(DelegateExecution execution) throws Exception {
    var externalSystemName = systemNameVariable.from(execution).getOrThrow();
    var methodName = methodNameVariable.from(execution).getOrThrow();

    var externalSystemConfiguration = getExternalSystemConfiguration(externalSystemName);
    var methodConfiguration = getMethodConfiguration(externalSystemName, methodName);

    var requestParameters =
        toMultiValueMapSeparatedByComma(requestParametersVariable.from(execution).get());
    var requestHeaders = new HttpHeaders(toMultiValueMapSeparatedByComma(
        requestHeadersVariable.from(execution).get()));
    var payload = payloadVariable.from(execution).getOptional().map(Object::toString).orElse(null);

    var auth = externalSystemConfiguration.getAuth();
    var secretData = kubernetesClient.secrets()
        .inNamespace(currentNamespace)
        .withName(auth.getSecretName()).get()
        .getData();

    authenticate(requestHeaders, auth, secretData);

    var uri = buildUri(externalSystemConfiguration.getUrl(), methodConfiguration.getPath(),
        requestParameters);
    var method = methodConfiguration.getMethod();
    var httpEntity = new HttpEntity<>(payload, requestHeaders);

    var responseValue = sendRequest(uri, method, httpEntity);

    responseVariable.on(execution).set(responseValue);
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

  private ExternalSystemConfigurationProperties.MethodConfiguration getMethodConfiguration(
      String externalSystemName, String methodName) {
    var externalSystemConfiguration = getExternalSystemConfiguration(externalSystemName);
    var method = externalSystemConfiguration.getMethods().get(methodName);
    if (Objects.isNull(method)) {
      throw new IllegalArgumentException(
          String.format("Method %s in external-system %s not configured", methodName,
              externalSystemName));
    }
    return method;
  }

  private void authenticate(HttpHeaders requestHeaders, AuthenticationConfiguration auth,
      Map<String, String> secretData) {
    if (auth.getType().equals(AuthenticationType.BASIC)) {
      authenticateWithBasic(requestHeaders, secretData);
    } else if (auth.getType().equals(AuthenticationType.PARTNER_TOKEN)) {
      authenticateWithPartnerToken(requestHeaders, auth, secretData);
    }
  }

  private void authenticateWithBasic(HttpHeaders requestHeaders, Map<String, String> secretData) {
    var username = decodeBase64(secretData.get(BASIC_SECRET_USERNAME_FIELD));
    var password = decodeBase64(secretData.get(BASIC_SECRET_PASSWORD_FIELD));
    requestHeaders.setBasicAuth(username, password);
  }

  private void authenticateWithPartnerToken(HttpHeaders requestHeaders,
      AuthenticationConfiguration auth, Map<String, String> secretData) {
    var partnerToken = decodeBase64(secretData.get(PARTNER_TOKEN_SECRET_TOKEN_FIELD));

    var uri = UriComponentsBuilder.fromUriString(auth.getPartnerTokenAuthUrl())
        .pathSegment(partnerToken)
        .build().toUri();
    var response = sendRequest(uri, HttpMethod.GET, null);
    var authToken = response.getResponseBody().jsonPath(auth.getTokenJsonPath()).stringValue();

    requestHeaders.setBearerAuth(authToken);
  }

  private String decodeBase64(String secret) {
    return new String(Base64.getDecoder().decode(secret));
  }
}
