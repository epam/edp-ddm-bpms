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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.epam.digital.data.platform.bpms.api.dto.HistoryUserTaskDto;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;

class HistoricTaskControllerIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/testGetExtendedTasks.bpmn")
  void shouldGetHistoryTaskWithProcessDefinitionName() throws Exception {
    var taskDefinitionKey = "Activity_046yw3m";

    var startResult = postForObject(
        "api/process-definition/key/testGetExtendedTasks_key/start", "", Map.class);
    var processId = (String) startResult.get("id");

    var taskId = taskService.createTaskQuery().processInstanceId(processId)
        .taskDefinitionKey(taskDefinitionKey).singleResult()
        .getId();
    postForNoContent(String.format("api/task/%s/complete", taskId), "{}");

    var historyUserTasks = postForObject("api/extended/history/task",
        "{\"assignee\": \"testuser\", \"finished\": true}", HistoryUserTaskDto[].class);

    var result = Arrays.stream(historyUserTasks)
        .filter(historyUserTaskDto -> processId.equals(historyUserTaskDto.getProcessInstanceId()))
        .collect(Collectors.toList());

    assertThat(result).isNotNull();
    assertThat(result.get(0).getAssignee()).isEqualTo("testuser");
    assertThat(result.get(0).getEndTime()).isNotNull();
    assertThat(result.get(0).getProcessDefinitionName()).isEqualTo("Test Name");
  }
}
