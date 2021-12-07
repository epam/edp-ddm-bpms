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

package com.epam.digital.data.platform.bpms.rest.it;


import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.bpms.api.dto.DdmSignableTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmTaskDto;
import com.epam.digital.data.platform.dso.api.dto.Subject;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;

class TaskControllerIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/testGetExtendedTasks.bpmn")
  void shouldGetTaskWithProcessDefinitionName() throws Exception {
    var startResult = postForObject("api/process-definition/key/testGetExtendedTasks_key/start", "",
        Map.class);
    var processId = (String) startResult.get("id");

    var result = postForObject("api/extended/task",
        "{\"assignee\": \"testuser\", \"processInstanceId\": \"" + processId + "\"}",
        DdmTaskDto[].class);

    assertThat(result).isNotNull();
    assertThat(result[0].getAssignee()).isEqualTo("testuser");
    assertThat(result[0].getProcessDefinitionName()).isNotNull();
    assertThat(result[0].getProcessDefinitionName()).isEqualTo("Test Name");
  }

  @Test
  void shouldGetTaskWithTaskProperties() throws Exception {
    var startResult = postForObject("api/process-definition/key/testGetExtendedTasks_key/start", "",
        Map.class);
    var processId = (String) startResult.get("id");
    var task = taskService.createTaskQuery().processInstanceId(processId).list().get(0);
    var taskId = task.getId();

    var result = getForObject("api/extended/task/" + taskId, DdmSignableTaskDto.class);

    assertThat(result.getProcessDefinitionName()).isEqualTo("Test Name");
    assertThat(result.getFormVariables()).hasSize(2)
        .containsAllEntriesOf(Map.of("formVariable", "var1", "formVariable2", "var2"));
    assertThat(result.getSignatureValidationPack()).hasSize(1).contains(Subject.ENTREPRENEUR);
    assertThat(result.isESign()).isTrue();
  }
}
