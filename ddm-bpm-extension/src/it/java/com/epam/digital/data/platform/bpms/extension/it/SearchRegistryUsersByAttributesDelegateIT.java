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

import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.jupiter.api.Test;

class SearchRegistryUsersByAttributesDelegateIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/connector/feature-search-registry-users-by-attributes.bpmn")
  void shouldGetOfficerUsersByAttributes() {
    mockConnectToKeycloak(officerRealm);
    mockKeycloakSearchUsersByAttributesV2(officerRealm,
        "/json/keycloak/searchOfficerRegistryUsersByAttributesRequest.json",
        "/json/keycloak/searchOfficerRegistryUsersByAttributesResponse.json");

    var processInstance = runtimeService.startProcessInstanceByKey(
        "feature-search-registry-users-by-attributes",
        Map.of(
            "isOfficer", true,
            "hierarchyCodeEquals", "100,100.200",
            "testAttributeEquals", "attr1",
            "hierarchyCodeStartsWith", "100",
            "testAttributeStartsWith", "a",
            "hierarchyCodeInverseStartsWith", "100.200.300.400,100.200.300.500",
            "testAttributeInverseStartsWith", "attr10101"
        ));

    BpmnAwareTests.assertThat(processInstance).isEnded()
        .variables().containsEntry("usersFormData",
            Map.of("users", List.of(
                Map.of("fullName", "Jane Doe", "edrpou", List.of("12345678"), "drfo",
                    List.of("1234567890")),
                Map.of("fullName", "John Doe", "edrpou", List.of("12345678"), "drfo",
                    List.of("1234567891"))
            )));
  }

  @Test
  @Deployment(resources = "bpmn/connector/feature-search-registry-users-by-attributes.bpmn")
  void shouldGetCitizenUsersByAttributes() {
    mockConnectToKeycloak(citizenRealm);
    mockKeycloakSearchUsersByAttributesV2(citizenRealm,
        "/json/keycloak/searchOfficerRegistryUsersByAttributesRequest.json",
        "/json/keycloak/searchOfficerRegistryUsersByAttributesResponse.json");

    var processInstance = runtimeService.startProcessInstanceByKey(
        "feature-search-registry-users-by-attributes",
        Map.of(
            "isOfficer", false,
            "hierarchyCodeEquals", "100,100.200",
            "testAttributeEquals", "attr1",
            "hierarchyCodeStartsWith", "100",
            "testAttributeStartsWith", "a",
            "hierarchyCodeInverseStartsWith", "100.200.300.400,100.200.300.500",
            "testAttributeInverseStartsWith", "attr10101"
        ));

    BpmnAwareTests.assertThat(processInstance).isEnded()
        .variables().containsEntry("usersFormData",
            Map.of("users", List.of(
                Map.of("fullName", "Jane Doe", "edrpou", List.of("12345678"), "drfo",
                    List.of("1234567890")),
                Map.of("fullName", "John Doe", "edrpou", List.of("12345678"), "drfo",
                    List.of("1234567891"))
            )));
  }
}
