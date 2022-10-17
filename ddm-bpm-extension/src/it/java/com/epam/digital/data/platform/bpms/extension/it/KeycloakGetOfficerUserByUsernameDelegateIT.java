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

class KeycloakGetOfficerUserByUsernameDelegateIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/connector/testKeycloakGetOfficerUserByUsernameDelegate.bpmn")
  void shouldGetUserByUsername() {
    mockConnectToKeycloak(officerRealm);
    mockKeycloakGetUserByUsername("testuser", officerRealm,
        "/json/keycloak/keycloakUserResponse.json");

    var processInstance = runtimeService.startProcessInstanceByKey("test_get_user_by_username");

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }
}
