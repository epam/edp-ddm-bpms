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

import com.epam.digital.data.platform.bpms.api.dto.DdmCompletedTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmLightweightTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmSignableTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmTaskDto;
import com.epam.digital.data.platform.dso.api.dto.Subject;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import lombok.SneakyThrows;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;

class TaskControllerIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/testGetExtendedTasks.bpmn")
  void shouldGetTaskWithProcessDefinitionName() throws Exception {
    var startResult = postForObject("api/process-definition/key/testGetExtendedTasks_key/start",
        "{\"businessKey\":\"businessKey\"}", Map.class);
    var processId = (String) startResult.get("id");

    var result = postForObject("api/extended/task",
        "{\"assignee\": \"testuser\", \"processInstanceId\": \"" + processId + "\"}",
        DdmTaskDto[].class);

    assertThat(result).isNotNull();
    assertThat(result[0].getAssignee()).isEqualTo("testuser");
    assertThat(result[0].getProcessDefinitionName()).isNotNull();
    assertThat(result[0].getProcessDefinitionName()).isEqualTo("Test Name");
    assertThat(result[0].getBusinessKey()).isEqualTo("businessKey");
  }

  @Test
  @Deployment(resources = {"bpmn/testParent.bpmn", "bpmn/testSubprocess1.bpmn",
      "bpmn/testSubprocess2.bpmn"})
  void shouldGetChildTaskWithParentProcessDefinitionName() throws JsonProcessingException {
    var startResult = postForObject("api/process-definition/key/parent/start",
        "{\"businessKey\":\"parent\"}", Map.class);

    var result = postForObject("api/extended/task",
        "{\"assignee\": \"testuser\"}",
        DdmTaskDto[].class);

    assertThat(result).isNotNull();
    assertThat(result[0].getTaskDefinitionKey()).isEqualTo("sub-process-2-task");
    assertThat(result[0].getProcessDefinitionName()).isEqualTo("Parent process");
  }

  @Test
  @Deployment(resources = {"bpmn/testParent.bpmn", "bpmn/testSubprocess1.bpmn",
      "bpmn/testSubprocess2.bpmn"})
  void shouldGetLightweightTasksWithCallActivities()
      throws JsonProcessingException {
    var startResult = postForObject("api/process-definition/key/parent/start",
        "{\"businessKey\":\"parent\"}", Map.class);
    var processId = (String) startResult.get("id");

    var result = postForObject("api/extended/task/lightweight",
        "{\"rootProcessInstanceId\": \"" + processId + "\"}",
        DdmTaskDto[].class);

    assertThat(result).isNotNull();
    assertThat(result.length).isOne();
    assertThat(result[0].getId()).isNotEmpty();
    assertThat(result[0].getAssignee()).isEqualTo("testuser");
  }

  @Test
  @Deployment(resources = {"bpmn/testParentMultiInstance.bpmn", "bpmn/testMultiInstanceCall.bpmn",
      "bpmn/testSubprocess2.bpmn"})
  void shouldGetLightweightTasksWithMultiInstanceCallActivity()
      throws JsonProcessingException {
    var startResult = postForObject("api/process-definition/key/parent_multi_instance/start",
        "{\"businessKey\":\"parent\"}", Map.class);
    var processId = (String) startResult.get("id");

    var result = postForObject("api/extended/task/lightweight",
        "{\"rootProcessInstanceId\": \"" + processId + "\"}",
        DdmTaskDto[].class);

    assertThat(result).isNotNull().hasSize(2);
    assertThat(result[0].getId()).isNotEmpty();
    assertThat(result[0].getAssignee()).isEqualTo("testuser");
    assertThat(result[1].getId()).isNotEmpty();
    assertThat(result[1].getAssignee()).isEqualTo("testuser");
  }

  @Test
  @Deployment(resources = "bpmn/testGetExtendedTasks.bpmn")
  void shouldGetLightweightTasksWithoutCallActivities() throws Exception {
    var startResult = postForObject("api/process-definition/key/testGetExtendedTasks_key/start",
        "{\"businessKey\":\"businessKey\"}", Map.class);
    var processId = (String) startResult.get("id");

    var result = postForObject("api/extended/task/lightweight",
        "{\"assignee\": \"testuser\", \"processInstanceId\": \"" + processId + "\"}",
        DdmLightweightTaskDto[].class);

    assertThat(result).isNotNull();
    assertThat(result[0].getId()).isNotEmpty();
    assertThat(result[0].getAssignee()).isEqualTo("testuser");
  }

  @Test
  @Deployment(resources = {"bpmn/testParent.bpmn", "bpmn/testSubprocess1.bpmn",
      "bpmn/testSubprocess2.bpmn"})
  void shouldCompleteTaskAndReturnCorrectRootProcessInstanceId() throws JsonProcessingException {
    var startResult = postForObject("api/process-definition/key/parent/start",
        "{\"businessKey\":\"parent\"}", Map.class);
    var processId = (String) startResult.get("id");

    var tasks = postForObject("api/extended/task/lightweight",
        "{\"rootProcessInstanceId\": \"" + processId + "\"}",
        DdmLightweightTaskDto[].class);
    var task = tasks[0];
    var result = postForObject("api/extended/task/" + task.getId() + "/complete", "{}",
        DdmCompletedTaskDto.class);

    assertThat(result).isNotNull();
    assertThat(result.getRootProcessInstanceId()).isEqualTo(processId);
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

  @Test
  @Deployment(resources = {"bpmn/testParent.bpmn", "bpmn/testSubprocess1.bpmn",
      "bpmn/testSubprocess2.bpmn"})
  @SneakyThrows
  void shouldGetTaskAndReturnCorrectRootProcessInstanceId() {
    var startResult = postForObject("api/process-definition/key/parent/start",
        "{\"businessKey\":\"parent\"}", Map.class);
    var processId = (String) startResult.get("id");

    var tasks = postForObject("api/extended/task/lightweight",
        "{\"rootProcessInstanceId\": \"" + processId + "\"}",
        DdmLightweightTaskDto[].class);
    var task = tasks[0];
    var result = getForObject("api/extended/task/" + task.getId(), DdmSignableTaskDto.class);

    assertThat(result).isNotNull();
    assertThat(result.getRootProcessInstanceId()).isEqualTo(processId);
  }

  @Deployment(resources = "bpmn/rootBpWith3Layers.bpmn")
  @Test
  void shouldReturnRootProcessInstanceIdAndEndedOnTaskCompletion() throws JsonProcessingException {
    var startResult = postForObject("api/process-definition/key/root_bp_with_3_layers/start", "",
        Map.class);
    var rootProcessInstanceId = (String) startResult.get("id");
    var task = taskService.createTaskQuery().taskDefinitionKey("user_task_on_layer_3")
        .singleResult();
    var taskId = task.getId();

    var result = postForObject("api/extended/task/" + taskId + "/complete", "{}",
        DdmCompletedTaskDto.class);

    assertThat(result).isNotNull()
        .hasFieldOrPropertyWithValue("id", taskId)
        .hasFieldOrPropertyWithValue("processInstanceId", task.getProcessInstanceId())
        .hasFieldOrPropertyWithValue("rootProcessInstanceId", rootProcessInstanceId)
        .hasFieldOrPropertyWithValue("rootProcessInstanceEnded", true)
        .hasFieldOrPropertyWithValue("variables", Map.of());
  }
}
