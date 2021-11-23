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

package com.epam.digital.data.platform.bpms.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class CompleterTaskEventListenerIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/completer_access_token_username.bpmn")
  public void shouldSetCompleterAccessTokenAndUsernameToVariables() throws Exception {
    var taskDefinitionKey = "Activity_test_completer_listener";
    var completerVarName = String.format("%s_completer", taskDefinitionKey);
    var result = postForObject("api/process-definition/key/completer_access_token_username/start",
        "{}", Map.class);

    var taskId = taskService.createTaskQuery().taskDefinitionKey(taskDefinitionKey).singleResult().getId();
    postForNoContent(String.format("api/task/%s/complete", taskId), "{}");

    var variables = runtimeService.getVariables((String) result.get("id"));
    var varName = (String) variables.get(completerVarName);
    assertThat(varName).isEqualTo("testuser");
  }
}
