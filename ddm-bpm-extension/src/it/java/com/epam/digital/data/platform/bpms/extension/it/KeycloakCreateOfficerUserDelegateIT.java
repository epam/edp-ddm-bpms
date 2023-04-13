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
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.epam.digital.data.platform.integration.idm.exception.KeycloakException;
import java.util.stream.Stream;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KeycloakCreateOfficerUserDelegateIT extends BaseIT {

  private static final String REQUEST_BODY_USER = "/json/keycloak/createKeycloakUserRequest.json";
  private static final String REQUEST_BODY_ROLES = "/json/keycloak/createKeycloakUserRequestBodyRolesToAdd.json";
  private static final String RESPONSE_BODY_ROLES = "/json/keycloak/createKeycloakUserRolesResponse.json";
  private static final String RESPONSE_BODY_USER = "/json/keycloak/createKeycloakUserResponse.json";

  @Test
  @Deployment(resources = {"bpmn/connector/testKeycloakCreateUser.bpmn"})
  void shouldCreateOfficerUser() {
    var requestAttributes = "{\"attributes\":{\"edrpou\":\"12345678\", \"drfo\":\"1234567890\", \"fullName\":\"Іванов Іван Іванович\"}}";
    var username = "5be6da054720ed166f4d65bb4f04299d7b60a3d3b1fd2485f32320b86562e135";
    mockConnectToKeycloak(officerRealm);
    mockKeycloakGetUsersByAttributes(officerRealm, requestAttributes,
        "/json/keycloak/keycloakUsersByAttributesEmptyResponse.json");
    mockKeycloakGetRoles();
    mockKeycloakCreateUser(officerRealm);
    mockKeycloakGetUserByUsername(username, officerRealm, RESPONSE_BODY_USER);
    mockKeycloakAddRoles();

    var processInstance = runtimeService.startProcessInstanceByKey(
        "test-create-keycloak-user");

    var userMappingsUrl = String.format("/auth/admin/realms/%s/users", officerRealm);
    var requestBody = convertJsonToString(REQUEST_BODY_USER);
    keycloakMockServer.verify(postRequestedFor(urlEqualTo(userMappingsUrl))
        .withRequestBody(equalToJson(requestBody)));
    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testKeycloakCreateUser.bpmn"})
  void shouldFailedCreateOfficerUserWhenUserWithTheSameAttributesExists() {
    var requestAttributes = "{\"attributes\":{\"edrpou\":\"12345678\", \"drfo\":\"1234567890\", \"fullName\":\"Іванов Іван Іванович\"}}";
    mockConnectToKeycloak(officerRealm);
    mockKeycloakGetUsersByAttributes(officerRealm, requestAttributes,
        "/json/keycloak/createKeycloakUserResponse.json");

    var ex = assertThrows(KeycloakException.class, () -> runtimeService
        .startProcessInstanceByKey("test-create-keycloak-user"));

    assertThat(ex).isNotNull();
    assertThat(ex.getMessage()).isEqualTo("Found 1 users with the same attributes");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCreateUserArguments")
  @Deployment(resources = {"bpmn/connector/testKeycloakSaveUserAttributes.bpmn"})
  void shouldThrowExceptionWhenCreateOfficerUserValidationFailed(String processDefinitionKey,
      String errorMsg) {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey(processDefinitionKey));

    assertThat(ex).isNotNull();
    assertThat(ex.getMessage()).isEqualTo(errorMsg);
  }

  static Stream<Arguments> testCreateUserArguments() {
    return Stream.of(
        arguments("test-create-keycloak-user-edrpou-duplicated",
            "Keycloak attribute [edrpou] is duplicated"),
        arguments("test-create-keycloak-user-invalid-custom-attributes",
            "Keycloak attribute name: te][t do not pass validation"),
        arguments("test-create-keycloak-user-invalid-drfo-value",
            "Value of the Keycloak attribute [drfo] do not match the regex: ^[ \\p{IsCyrillic}\\p{IsLatin}\\d]{1,10}$"),
        arguments("test-create-keycloak-user-invalid-edrpou-value",
            "Value of the Keycloak attribute [edrpou] do not match the regex: ^\\d{8}(?:\\d{2})?$"),
        arguments("test-create-keycloak-user-invalid-full-name-value",
            "Value of the Keycloak attribute [fullName] do not match the regex: ^[ '`’—–\\-\\p{IsCyrillic}\\p{IsLatin}\\d]{1,255}$"),
        arguments("test-create-keycloak-user-edrpou-not-defined", "Variable edrpou not found"),
        arguments("test-create-keycloak-user-drfo-not-defined", "Variable drfo not found"),
        arguments("test-create-keycloak-user-full-name-not-defined", "Variable fullName not found")
    );
  }

  private void mockKeycloakCreateUser(String realm) {
    var roleMappingsUrl = String.format("/auth/admin/realms/%s/users", realm);
    keycloakMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo(roleMappingsUrl)).withRequestBody(
                equalToJson(convertJsonToString(
                    KeycloakCreateOfficerUserDelegateIT.REQUEST_BODY_USER)))
            .willReturn(aResponse().withStatus(201))));
  }

  private void mockKeycloakGetRoles() {
    keycloakMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/auth/admin/realms/officer-realm/roles"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(convertJsonToString(RESPONSE_BODY_ROLES)))));
  }

  private void mockKeycloakAddRoles() {
    var roleMappingsUrl = String
        .format("/auth/admin/realms/officer-realm/users/%s/role-mappings/realm",
            "7004ebde-68cf-4e25-bb76-b1642a3814e5");

    keycloakMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo(roleMappingsUrl)).withRequestBody(
                equalToJson(
                    convertJsonToString(KeycloakCreateOfficerUserDelegateIT.REQUEST_BODY_ROLES)))
            .willReturn(aResponse().withStatus(200))));
  }
}
