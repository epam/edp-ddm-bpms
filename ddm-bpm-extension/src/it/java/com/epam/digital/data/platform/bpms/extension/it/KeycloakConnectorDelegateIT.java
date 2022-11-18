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

package com.epam.digital.data.platform.bpms.extension.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.epam.digital.data.platform.integration.idm.exception.KeycloakException;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;

public class KeycloakConnectorDelegateIT extends BaseIT {

  private static final String RESPONSE_BODY_ROLE = "/json/keycloak/keycloakRoleResponse.json";
  private static final String RESPONSE_BODY_USER = "/json/keycloak/keycloakUserResponse.json";

  @Test
  @Deployment(resources = {"bpmn/connector/testAddRoleKeycloak.bpmn"})
  public void shouldAddRealmRoleToKeycloak() {
    mockConnectToKeycloak(citizenRealm);
    mockKeycloakGetUserByUsername("testuser", citizenRealm, RESPONSE_BODY_USER);
    mockKeycloakGetRole("citizen", RESPONSE_BODY_ROLE, 200);
    mockKeycloakAddRole("7004ebde-68cf-4e25-bb76-b1642a3814e5",
        "/json/keycloak/keycloakRequestBodyRoles.json");

    var processInstance = runtimeService.startProcessInstanceByKey("testAddRoleKeycloak_key");

    var roleMappingsUrl = "/auth/admin/realms/citizen-realm/users/7004ebde-68cf-4e25-bb76-b1642a3814e5/role-mappings/realm";
    var requestBodyRoles = convertJsonToString("/json/keycloak/keycloakRequestBodyRoles.json");
    keycloakMockServer.verify(1,
        postRequestedFor(urlEqualTo(roleMappingsUrl)).withRequestBody(equalToJson(requestBodyRoles)));

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testRemoveRoleKeycloak.bpmn"})
  public void shouldRemoveRealmRoleFromKeycloak() {
    mockConnectToKeycloak(citizenRealm);
    mockKeycloakGetUserByUsername("testuser", citizenRealm, RESPONSE_BODY_USER);
    mockKeycloakGetRole("citizen", RESPONSE_BODY_ROLE, 200);
    mockKeycloakDeleteRole("7004ebde-68cf-4e25-bb76-b1642a3814e5",
        "/json/keycloak/keycloakRequestBodyRoles.json");

    var processInstance = runtimeService.startProcessInstanceByKey("testRemoveKeycloakRoleKey");

    var roleMappingsUrl = "/auth/admin/realms/citizen-realm/users/7004ebde-68cf-4e25-bb76-b1642a3814e5/role-mappings/realm";
    var requestBodyRoles = convertJsonToString("/json/keycloak/keycloakRequestBodyRoles.json");
    keycloakMockServer.verify(1,
        deleteRequestedFor(urlEqualTo(roleMappingsUrl)).withRequestBody(equalToJson(requestBodyRoles)));

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testAddRoleKeycloak.bpmn"})
  public void shouldGetExceptionWhenRoleNotFound() {
    mockConnectToKeycloak(citizenRealm);
    mockKeycloakGetRole("citizen", "", 404);

    var ex = assertThrows(KeycloakException.class, () -> runtimeService
        .startProcessInstanceByKey("testAddRoleKeycloak_key"));

    assertThat(ex).isNotNull();
    assertThat(ex.getMessage()).isEqualTo("Couldn't find role citizen in realm citizen-realm");
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testAddRoleKeycloak.bpmn"})
  public void shouldGetExceptionWhenUserNotFound() {
    mockConnectToKeycloak(citizenRealm);
    mockKeycloakGetUserByUsername("testuser", citizenRealm, "[]");
    mockKeycloakGetRole("citizen", RESPONSE_BODY_ROLE, 200);

    var ex = assertThrows(KeycloakException.class, () -> runtimeService
        .startProcessInstanceByKey("testAddRoleKeycloak_key"));

    assertThat(ex).isNotNull();
    assertThat(ex.getMessage()).isEqualTo("Found 0 users with name testuser, but expect one");
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testGetRolesKeycloak.bpmn"})
  public void shouldGetRegulationsRolesFromKeycloak() {
    var getRolesUrl = "/auth/admin/realms/citizen-realm/roles";
    mockConnectToKeycloak(citizenRealm);

    keycloakMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo(getRolesUrl))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(convertJsonToString("/json/keycloak/keycloakRolesResponse.json")))));

    var processInstance = runtimeService.startProcessInstanceByKey("testGetKeycloakRoles_key", "");

    keycloakMockServer.verify(1, getRequestedFor(urlEqualTo(getRolesUrl)));
    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testGetUsersByDefinedRoleKeycloak.bpmn"})
  public void shouldGetUsersByRoleFromKeycloak() {
    var getUsersUrl = "/auth/admin/realms/officer-realm/roles/test-role-name/users";
    mockConnectToKeycloak(officerRealm);
    mockKeycloakGetUsersByRole("test-role-name",
        "/json/keycloak/keycloakUsersByDefinedRoleResponse.json");

    var processInstance = runtimeService.startProcessInstanceByKey("testGetUsersByDefinedRoleKey");

    keycloakMockServer.verify(1, getRequestedFor(urlEqualTo(getUsersUrl)));
    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testGetUsersByNotDefinedRoleKeycloak.bpmn"})
  public void shouldGetUsersByDefaultRoleFromKeycloak() {
    var getUsersUrl = "/auth/admin/realms/officer-realm/roles/officer/users";
    mockConnectToKeycloak(officerRealm);
    mockKeycloakGetUsersByRole("officer",
        "/json/keycloak/keycloakUsersByNotDefinedRoleResponse.json");

    var processInstance = runtimeService.startProcessInstanceByKey(
        "testGetUsersByNotDefinedRoleKey");

    keycloakMockServer.verify(1, getRequestedFor(urlEqualTo(getUsersUrl)));
    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "bpmn/connector/getKeycloakUsersByAttribute.bpmn")
  public void testGetUsersFromKeycloakByAttributes() {
    var requestAttributes = "{\"attributes\":{\"edrpou\":\"12345678\"}}";
    mockConnectToKeycloak(officerRealm);
    mockKeycloakGetUsersByAttributes(officerRealm, requestAttributes,
        "/json/keycloak/keycloakUsersByAttributesResponse.json");

    var processInstance = runtimeService.startProcessInstanceByKey("testGetUsersByAttributes_key");

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "bpmn/connector/getKeycloakCitizenUsersByAttributeEdrpou.bpmn")
  public void testGeyCitizenUsersFromKeycloakByAttributesEdrpou() {
    var requestAttributes = "{\"attributes\":{\"edrpou\":\"12345678\"}}";
    mockConnectToKeycloak(citizenRealm);
    mockKeycloakGetUsersByAttributes(citizenRealm, requestAttributes,
        "/json/keycloak/keycloakUsersByAttributesResponse.json");

    var processInstance = runtimeService.startProcessInstanceByKey("testGetCitizenUsersByAttributesEdrpou_key");

    var roleMappingsUrl = "/auth/realms/citizen-realm/users/search";
    keycloakMockServer.verify(1,
        postRequestedFor(urlEqualTo(roleMappingsUrl)).withRequestBody(
            equalToJson(requestAttributes)));

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "bpmn/connector/getKeycloakCitizenUsersByAttributeDrfo.bpmn")
  public void testGeyCitizenUsersFromKeycloakByAttributesDrfo() {
    var requestAttributes = "{\"attributes\":{\"drfo\":\"12345678\"}}";
    mockConnectToKeycloak(citizenRealm);
    mockKeycloakGetUsersByAttributes(citizenRealm, requestAttributes,
        "/json/keycloak/keycloakUsersByAttributesResponse.json");

    var processInstance = runtimeService.startProcessInstanceByKey("testGetCitizenUsersByAttributesDrfo_key");

    var roleMappingsUrl = "/auth/realms/citizen-realm/users/search";
    keycloakMockServer.verify(1,
        postRequestedFor(urlEqualTo(roleMappingsUrl)).withRequestBody(
            equalToJson(requestAttributes)));

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  private void mockKeycloakGetUsersByRole(String role, String response) {
    var getUsersUrl = String.format("/auth/admin/realms/officer-realm/roles/%s/users", role);
    keycloakMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo(getUsersUrl))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(convertJsonToString(response)))));
  }

  private void mockKeycloakGetRole(String role, String responseBody, int status) {
    keycloakMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/auth/admin/realms/citizen-realm/roles/" + role))
            .willReturn(aResponse().withStatus(status)
                .withHeader("Content-type", "application/json")
                .withBody(convertJsonToString(responseBody)))));
  }

  private void mockKeycloakAddRole(String userId, String request) {
    var roleMappingsUrl = String
        .format("/auth/admin/realms/citizen-realm/users/%s/role-mappings/realm", userId);

    keycloakMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo(roleMappingsUrl)).withRequestBody(
            equalToJson(convertJsonToString(request))).willReturn(aResponse().withStatus(200))));
  }

  private void mockKeycloakDeleteRole(String userId, String request) {
    var roleMappingsUrl = String
        .format("/auth/admin/realms/citizen-realm/users/%s/role-mappings/realm", userId);

    keycloakMockServer.addStubMapping(
        stubFor(delete(urlPathEqualTo(roleMappingsUrl)).withRequestBody(
            equalToJson(convertJsonToString(request))).willReturn(aResponse().withStatus(200))));
  }
}
