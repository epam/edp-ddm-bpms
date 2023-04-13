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

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
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

class KeycloakSaveOfficerUserAttributesDelegateIT extends BaseIT {

  private static final String RESPONSE_BODY_USER = "/json/keycloak/keycloakUserResponse.json";
  private static final String REQUEST_BODY_USER = "/json/keycloak/keycloakUserRequest.json";
  private static final String REQUEST_BODY_TEST_USER = "/json/keycloak/keycloakTestUserRequest.json";

  @Test
  @Deployment(resources = {"bpmn/connector/testKeycloakSaveUserAttributes.bpmn"})
  void shouldSaveOfficerUserAttributes() {
    var requestAttributes = "{\"attributes\":{\"edrpou\":\"15250623\", \"drfo\":\"1525062300\", \"fullName\":\"Сидоренко Василь Леонідович\"}}";
    var userId = "7004ebde-68cf-4e25-bb76-b1642a3814e5";
    mockConnectToKeycloak(officerRealm);
    mockKeycloakGetUsersByAttributes(officerRealm, requestAttributes,
        "/json/keycloak/keycloakUsersByAttributesEmptyResponse.json");
    mockKeycloakGetUserByUsername("testuser", officerRealm, RESPONSE_BODY_USER);
    mockGetKeycloakGetUserById(userId, officerRealm,
        "/json/keycloak/keycloakUserByIdResponse.json");
    mockKeycloakUpdateUser(userId, officerRealm, REQUEST_BODY_USER);

    var processInstance = runtimeService.startProcessInstanceByKey(
        "test-save-keycloak-user-attributes");

    var userMappingsUrl = String.format("/auth/admin/realms/%s/users/%s", officerRealm, userId);
    var requestBody = convertJsonToString(REQUEST_BODY_USER);
    keycloakMockServer.verify(putRequestedFor(urlEqualTo(userMappingsUrl))
        .withRequestBody(equalToJson(requestBody)));
    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testKeycloakSaveUserAttributes.bpmn"})
  void shouldSaveOfficerUserAttributesWithoutAnyDefinedAttributes() {
    var userId = "7004ebde-68cf-4e25-bb76-b1642a3814e5";
    mockConnectToKeycloak(officerRealm);
    mockKeycloakGetUserByUsername("testuser", officerRealm, RESPONSE_BODY_USER);
    mockGetKeycloakGetUserById(userId, officerRealm,
        "/json/keycloak/keycloakUserByIdResponse.json");
    mockKeycloakUpdateUser(userId, officerRealm, REQUEST_BODY_TEST_USER);

    var processInstance = runtimeService.startProcessInstanceByKey(
        "test-save-keycloak-user-attributes-empty-attributes");

    var userMappingsUrl = String.format("/auth/admin/realms/%s/users/%s", officerRealm, userId);
    var requestBody = convertJsonToString(REQUEST_BODY_TEST_USER);
    keycloakMockServer.verify(putRequestedFor(urlEqualTo(userMappingsUrl))
        .withRequestBody(equalToJson(requestBody)));
    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testKeycloakSaveUserAttributes.bpmn"})
  void shouldFailedSaveOfficerUserAttributesWhenUserWithTheSameAttributesExists() {
    var requestAttributes = "{\"attributes\":{\"edrpou\":\"15250623\", \"fullName\":\"fullname\"}}";
    mockConnectToKeycloak(officerRealm);
    mockKeycloakGetUserByUsername("testuser", officerRealm, RESPONSE_BODY_USER);
    mockKeycloakGetUsersByAttributes(officerRealm, requestAttributes,
        "/json/keycloak/keycloakSaveAttributesUserResponse.json");

    var ex = assertThrows(KeycloakException.class, () -> runtimeService
        .startProcessInstanceByKey("test-save-keycloak-user-attributes-duplicated-user"));

    assertThat(ex).isNotNull();
    assertThat(ex.getMessage()).isEqualTo("Found 1 users with the same attributes");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testSaveUserAttributesArguments")
  @Deployment(resources = {"bpmn/connector/testKeycloakSaveUserAttributes.bpmn"})
  void shouldThrowExceptionWhenSaveOfficerUserAttributesValidationFailed(
      String processDefinitionKey, String errorMsg) {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey(processDefinitionKey));

    assertThat(ex).isNotNull();
    assertThat(ex.getMessage()).isEqualTo(errorMsg);
  }

  static Stream<Arguments> testSaveUserAttributesArguments() {
    return Stream.of(
        arguments("test-save-keycloak-user-attributes-drfo-duplicated",
            "Keycloak attribute [drfo] is duplicated"),
        arguments("test-save-keycloak-user-attributes-invalid-custom-attributes",
            "Keycloak attribute age: {22} do not pass validation"),
        arguments("test-save-keycloak-user-attributes-invalid-drfo-value",
            "Value of the Keycloak attribute [drfo] do not match the regex: ^[ \\p{IsCyrillic}\\p{IsLatin}\\d]{1,10}$"),
        arguments("test-save-keycloak-user-attributes-invalid-edrpou-value",
            "Value of the Keycloak attribute [edrpou] do not match the regex: ^\\d{8}(?:\\d{2})?$"),
        arguments("test-save-keycloak-user-attributes-invalid-full-name-value",
            "Value of the Keycloak attribute [fullName] do not match the regex: ^[ '`’—–\\-\\p{IsCyrillic}\\p{IsLatin}\\d]{1,255}$")
    );
  }
}
