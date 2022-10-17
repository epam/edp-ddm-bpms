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

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.jupiter.api.Test;

class KeycloakSaveOfficerAttributeDelegateIT extends BaseIT {

  @Test
  @Deployment(resources = {"bpmn/connector/testKeycloakSaveOfficerAttributeDelegate.bpmn"})
  void shouldSaveAttribute() {
    var userId = "7004ebde-68cf-4e25-bb76-b1642a3814e5";
    mockConnectToKeycloak(officerSystemClientRealm);
    mockKeycloakGetUserByUsername("testuser", officerSystemClientRealm,
        "/json/keycloak/keycloakUserResponse.json");
    mockGetKeycloakGetUserById(userId, officerSystemClientRealm,
        "/json/keycloak/keycloakUserByIdResponse.json");
    mockKeycloakUpdateUser(userId, officerSystemClientRealm,
        "/json/keycloak/keycloakUpdateUserWithAttributesRequestBody.json");

    var processInstance = runtimeService.startProcessInstanceByKey("test_save_attribute");

    var userMappingsUrl = String.format("/auth/admin/realms/%s/users/%s", officerSystemClientRealm,
        userId);
    var requestBodyRoles = convertJsonToString(
        "/json/keycloak/keycloakUpdateUserWithAttributesRequestBody.json");
    keycloakMockServer.verify(1, putRequestedFor(urlEqualTo(userMappingsUrl))
        .withRequestBody(equalToJson(requestBodyRoles)));
    BpmnAwareTests.assertThat(processInstance).isEnded();
  }
}
