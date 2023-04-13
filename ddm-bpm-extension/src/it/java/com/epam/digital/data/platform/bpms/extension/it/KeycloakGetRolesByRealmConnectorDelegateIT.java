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

import java.util.ArrayList;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessInstanceWithVariablesImpl;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;

public class KeycloakGetRolesByRealmConnectorDelegateIT extends BaseIT {

  private static final String RESPONSE_BODY_ROLES = "/json/keycloak/keycloakSaveUserRoleRolesResponse.json";
  private static final String RESPONSE_BODY_USERS_ROLES_EMPTY = "/json/keycloak/keycloakEmptyRequestBodyRoles.json";
  @Test
  @Deployment(resources = {"bpmn/connector/testKeycloakGetRolesByRealm.bpmn"})
  public void shouldReturnKeycloakRolesByRealm() {
    mockConnectToKeycloak(officerRealm);
    mockKeycloakGetRoles(RESPONSE_BODY_ROLES);
    var processInstance = runtimeService.startProcessInstanceByKey("get-keycloak-roles-test");
    var response = ((ProcessInstanceWithVariablesImpl) processInstance).getVariables()
        .getValue("response", ArrayList.class);

    Assertions.assertThat(response.contains("test_role1")).isTrue();
    Assertions.assertThat(response.contains("test_role2")).isTrue();
    Assertions.assertThat(response.contains("test_role3")).isTrue();
    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testKeycloakGetRolesByRealm.bpmn"})
  public void shouldReturnEmptyKeycloakRolesByRealm() {
    mockConnectToKeycloak(officerRealm);
    mockKeycloakGetRoles(RESPONSE_BODY_USERS_ROLES_EMPTY);
    var processInstance = runtimeService.startProcessInstanceByKey("get-keycloak-empty-roles-test");
    var response = ((ProcessInstanceWithVariablesImpl) processInstance).getVariables()
        .getValue("response", ArrayList.class);

    Assertions.assertThat(response).isNotNull();
    Assertions.assertThat(response).isEmpty();

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  private void mockKeycloakGetRoles(String responseBody) {
    keycloakMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/auth/admin/realms/officer-realm/roles"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(convertJsonToString(responseBody)))));
  }
}
