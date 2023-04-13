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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessInstanceWithVariablesImpl;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class KeycloakGetUserRoleConnectorDelegateIT extends BaseIT {
  private static final String RESPONSE_BODY_USERS = "/json/keycloak/keycloakUserResponse.json";
  private static final String RESPONSE_BODY_USERS_ROLES = "/json/keycloak/keycloakUsersRolesResponse.json";
  private static final String RESPONSE_BODY_USERS_ROLES_EMPTY = "/json/keycloak/keycloakEmptyRequestBodyRoles.json";

  @Test
  @Deployment(resources = {"bpmn/connector/testKeycloakGetRoles.bpmn"})
  public void shouldGetKeycloakRolesFromUser() {
    mockConnectToKeycloak(officerRealm);
    mockKeycloakGetUserByUsername("testuser", officerRealm, RESPONSE_BODY_USERS);
    mockKeycloakGetUserRoles("7004ebde-68cf-4e25-bb76-b1642a3814e5", RESPONSE_BODY_USERS_ROLES);
    var processInstance = runtimeService.startProcessInstanceByKey(
        "get-user-roles-test");
    var response = ((ProcessInstanceWithVariablesImpl) processInstance).getVariables()
        .getValue("response", ArrayList.class).get(0);

    Assertions.assertEquals(response, "death-officer");

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testKeycloakGetRoles.bpmn"})
  public void shouldThrowExceptionWhenUsernameIsEmpty() {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey("get-user-roles-empty-username-test"));

    assertThat(ex).isNotNull();
    assertThat(ex.getMessage()).isEqualTo(
        "Variable username not found");
  }

  @Test
  public void shouldReturnEmptyListWhenUserHasNoRoles(){
    mockConnectToKeycloak(officerRealm);
    mockKeycloakGetUserByUsername("testuser", officerRealm, RESPONSE_BODY_USERS);
    mockKeycloakGetUserRoles("7004ebde-68cf-4e25-bb76-b1642a3814e5", RESPONSE_BODY_USERS_ROLES_EMPTY);
    var processInstance = runtimeService.startProcessInstanceByKey(
        "get-user-roles-empty-user-roles-test");
    var response = ((ProcessInstanceWithVariablesImpl) processInstance).getVariables()
        .getValue("response", ArrayList.class);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(response.size(), 0);

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  private void mockKeycloakGetUserRoles(String userId, String response) {
    keycloakMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo(
            String.format("/auth/admin/realms/officer-realm/users/%s/role-mappings/realm", userId)))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(convertJsonToString(response)))));
  }
}
