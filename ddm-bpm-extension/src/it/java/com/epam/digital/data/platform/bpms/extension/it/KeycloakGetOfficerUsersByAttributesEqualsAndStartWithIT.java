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

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.jupiter.api.Test;

class KeycloakGetOfficerUsersByAttributesEqualsAndStartWithIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/connector/testKeycloakGetOfficerUsersByAttributesEqualsAndStartWith.bpmn")
  void shouldGetOfficerUsersByAttributes() {
    var requestAttributes = "{'attributesEquals':{'edrpou':'12345678'},'attributesStartsWith':{'KATOTTG':['UA03']}}";
    mockConnectToKeycloak(officerRealm);
    mockKeycloakSearchUsersByAttributes(officerRealm, requestAttributes,
        "/json/keycloak/keycloakUsersByAttributesResponse.json");

    var processInstance = runtimeService.startProcessInstanceByKey("test_start_with");

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }
}
