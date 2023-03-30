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

package com.epam.digital.data.platform.bpms.extension.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.epam.digital.data.platform.bpms.extension.config.properties.ExternalSystemConfigurationProperties.AuthenticationConfiguration.AuthenticationType;
import com.epam.digital.data.platform.bpms.extension.exception.AuthConfigurationException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import javax.inject.Inject;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;

public class ExternalSystemConnectorDelegateIT extends BaseIT {

  @Inject
  @Qualifier("external-system")
  private WireMockServer externalSystem1;

  @Inject
  private CacheManager cacheManager;

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegate.bpmn")
  public void externalSystemConnectorNoSystemNameDefined() {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey("external_system_connector_no_system_name_defined"));
    assertThat(ex.getMessage()).isEqualTo("Variable systemName not found");
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegate.bpmn")
  public void externalSystemConnectorNoOperationNameDefined() {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey("external_system_connector_no_operation_name_defined"));
    assertThat(ex.getMessage()).isEqualTo(
        "Variable not found. One of 'operationName' or 'methodName' must be specified.");
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegate.bpmn")
  public void externalSystemConnectorWrongSystemNameDefined() {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey("external_system_connector_wrong_system_name_defined"));
    assertThat(ex.getMessage()).isEqualTo("External-system with name system not configured");
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegate.bpmn")
  public void externalSystemConnectorWrongOperationNameDefined() {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey("external_system_connector_wrong_operation_name_defined"));
    assertThat(ex.getMessage()).isEqualTo(
        "Operation operation in external-system system1 not configured");
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegate.bpmn")
  public void externalSystemConnectorOperation1() {
    externalSystem1.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/get"))
            .withHeader("Authorization", equalTo("Basic dXNlcjpjR0Z6Y3c="))
            .willReturn(aResponse().withStatus(200))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("external_system_connector_operation_1");

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegate.bpmn")
  public void externalSystemPartnerTokenAuth() {
    externalSystem1.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/auth/partner/token"))
            .willReturn(aResponse().withStatus(200).withBody("{\"token\":\"bearer-token\"}"))));
    externalSystem1.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/get"))
            .withHeader("Authorization", equalTo("Bearer bearer-token"))
            .willReturn(aResponse().withStatus(200))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("external_system_partner_token_auth");

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegate.bpmn")
  public void externalSystemCachedPartnerTokenAuth() {
    var jwt = generateJWT(Instant.now().plus(1, ChronoUnit.HOURS));
    var cache = cacheManager.getCache(AuthenticationType.AUTH_TOKEN_BEARER.getCode());

    Assertions.assertThat(cache).isNotNull();

    cache.put(externalSystem1.baseUrl(), jwt);

    externalSystem1.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/get"))
            .withHeader("Authorization", equalTo("Bearer " + jwt))
            .willReturn(aResponse().withStatus(200))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("external_system_partner_token_auth");

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegate.bpmn")
  public void externalSystemBearerAuth() {
    externalSystem1.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/get-with-bearer"))
            .withHeader("Authorization", equalTo("Bearer bearer-token"))
            .willReturn(aResponse().withStatus(200))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("external_system_bearer_auth");

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegate.bpmn")
  public void externalSystemAuthToken() {
    externalSystem1.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/get-with-auth-token"))
            .withHeader("Authorization", equalTo("Bearer auth-token"))
            .willReturn(aResponse().withStatus(200))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("external_system_auth_token");

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegateFailedAuthConfig.bpmn")
  public void externalSystemConnectorBasicWithoutUsername() {
    var ex = assertThrows(AuthConfigurationException.class, () -> runtimeService
        .startProcessInstanceByKey("external_system_connector_basic_without_username"));
    assertThat(ex.getMessage()).isEqualTo(
        "Authentication configuration for external-system with name system-basic-without-username not configured");
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegateFailedAuthConfig.bpmn")
  public void externalSystemConnectorBearerWithoutSecret() {
    var ex = assertThrows(AuthConfigurationException.class, () -> runtimeService
        .startProcessInstanceByKey("external_system_connector_bearer_without_secret"));
    assertThat(ex.getMessage()).isEqualTo(
        "Authentication configuration for external-system with name system-bearer-without-secret not configured");
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegateFailedAuthConfig.bpmn")
  public void externalSystemConnectorAuthTokenWithoutToken() {
    var ex = assertThrows(AuthConfigurationException.class, () -> runtimeService
        .startProcessInstanceByKey("external_system_connector_auth_token_without_token"));
    assertThat(ex.getMessage()).isEqualTo(
        "Authentication configuration for external-system with name system-auth-token-without-token not configured");
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegate.bpmn")
  public void externalSystemPartnerTokenAuthRelativePath() {
    externalSystem1.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/auth/partner/token"))
            .willReturn(aResponse().withStatus(200).withBody("{\"token\":\"bearer-token\"}"))));
    externalSystem1.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/get"))
            .withHeader("Authorization", equalTo("Bearer bearer-token"))
            .willReturn(aResponse().withStatus(200))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("external_system_partner_token_auth_relative_path");

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegate.bpmn")
  public void externalSystemWithoutOperations() {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey("external_system_without_operations"));
    assertThat(ex.getMessage()).isEqualTo(
        "Operation operation1 in external-system system-without-operations not configured");
  }

  @SneakyThrows
  private String generateJWT(Instant instant) {
    var key = new ECKeyGenerator(Curve.SECP256K1)
        .keyID(UUID.randomUUID().toString())
        .generate();

    var header = new JWSHeader.Builder(JWSAlgorithm.ES256K)
        .type(JOSEObjectType.JWT)
        .keyID(key.getKeyID())
        .build();
    var payload = new JWTClaimsSet.Builder()
        .issuer("http://keycloak:8080")
        .subject("admin_user")
        .expirationTime(Date.from(instant))
        .build();

    var signedJWT = new SignedJWT(header, payload);
    signedJWT.sign(new ECDSASigner(key.toECPrivateKey()));
    return signedJWT.serialize();
  }
}