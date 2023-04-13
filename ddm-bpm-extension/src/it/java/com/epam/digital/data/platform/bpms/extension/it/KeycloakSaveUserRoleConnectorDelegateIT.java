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
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;

public class KeycloakSaveUserRoleConnectorDelegateIT extends BaseIT {

  private static final String RESPONSE_BODY_ROLES = "/json/keycloak/keycloakSaveUserRoleRolesResponse.json";
  private static final String REQUEST_BODY_ROLES_TO_ADD = "/json/keycloak/keycloakRequestBodyRolesToAdd.json";
  private static final String REQUEST_EMPTY_BODY_ROLES = "/json/keycloak/keycloakEmptyRequestBodyRoles.json";
  private static final String REQUEST_BODY_ROLES = "/json/keycloak/keycloakSaveUserRoleRequestBodyRoles.json";
  private static final String RESPONSE_BODY_USER = "/json/keycloak/keycloakUserResponse.json";

  @Test
  @Deployment(resources = {"bpmn/connector/testKeycloakSaveRoles.bpmn"})
  public void shouldAddRegistryRolesToKeycloakUser() {
    var userId = "7004ebde-68cf-4e25-bb76-b1642a3814e5";
    mockConnectToKeycloak(officerRealm);
    mockKeycloakGetRoles();
    mockKeycloakGetUserByUsername("testuser", officerRealm, RESPONSE_BODY_USER);
    mockKeycloakDeleteRoles(userId);
    mockKeycloakAddRoles(userId, REQUEST_BODY_ROLES_TO_ADD);

    var processInstance = runtimeService.startProcessInstanceByKey("test-save-keycloak-roles");

    var roleMappingsUrl = "/auth/admin/realms/officer-realm/users/7004ebde-68cf-4e25-bb76-b1642a3814e5/role-mappings/realm";
    var requestBodyRoles = convertJsonToString(REQUEST_BODY_ROLES_TO_ADD);
    keycloakMockServer.verify(1,
        postRequestedFor(urlEqualTo(roleMappingsUrl)).withRequestBody(
            equalToJson(requestBodyRoles)));

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testKeycloakSaveRoles.bpmn"})
  public void shouldJustRemoveRolesFromKeycloakUserWhenInputRolesIsEmpty() {
    var userId = "7004ebde-68cf-4e25-bb76-b1642a3814e5";
    mockConnectToKeycloak(officerRealm);
    mockKeycloakGetRoles();
    mockKeycloakGetUserByUsername("testuser", officerRealm, RESPONSE_BODY_USER);
    mockKeycloakDeleteRoles(userId);
    mockKeycloakAddRoles(userId, REQUEST_EMPTY_BODY_ROLES);

    var processInstance = runtimeService.startProcessInstanceByKey(
        "test-save-keycloak-roles-empty-roles");

    var roleMappingsUrl = "/auth/admin/realms/officer-realm/users/7004ebde-68cf-4e25-bb76-b1642a3814e5/role-mappings/realm";
    var requestBodyRoles = convertJsonToString(REQUEST_EMPTY_BODY_ROLES);
    keycloakMockServer.verify(1,
        postRequestedFor(urlEqualTo(roleMappingsUrl)).withRequestBody(
            equalToJson(requestBodyRoles)));

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testKeycloakSaveRoles.bpmn"})
  public void shouldGetExceptionWhenRolesDoNotMatch() {
    mockConnectToKeycloak(officerRealm);
    mockKeycloakGetRoles();

    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey("test-save-keycloak-roles-do-not-match"));

    assertThat(ex).isNotNull();
    assertThat(ex.getMessage()).isEqualTo(
        "Input roles: [test_role11, test_role22] do not match the selected type: [REGISTRY ROLES]");
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testKeycloakSaveRoles.bpmn"})
  public void shouldGetExceptionWhenUserNameNotDefined() {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey("test-save-keycloak-roles-empty-username"));

    assertThat(ex).isNotNull();
    assertThat(ex.getMessage()).isEqualTo(
        "Variable username not found");
  }

  private void mockKeycloakGetRoles() {
    keycloakMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/auth/admin/realms/officer-realm/roles"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(convertJsonToString(RESPONSE_BODY_ROLES)))));
  }

  private void mockKeycloakAddRoles(String userId, String request) {
    var roleMappingsUrl = String
        .format("/auth/admin/realms/officer-realm/users/%s/role-mappings/realm", userId);

    keycloakMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo(roleMappingsUrl)).withRequestBody(
            equalToJson(convertJsonToString(request))).willReturn(aResponse().withStatus(200))));
  }

  private void mockKeycloakDeleteRoles(String userId) {
    var roleMappingsUrl = String
        .format("/auth/admin/realms/officer-realm/users/%s/role-mappings/realm", userId);

    keycloakMockServer.addStubMapping(
        stubFor(delete(urlPathEqualTo(roleMappingsUrl)).withRequestBody(
                equalToJson(convertJsonToString(REQUEST_BODY_ROLES)))
            .willReturn(aResponse().withStatus(200))));
  }
}
