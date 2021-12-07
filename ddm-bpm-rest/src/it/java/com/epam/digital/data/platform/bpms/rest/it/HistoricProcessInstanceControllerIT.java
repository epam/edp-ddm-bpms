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

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.HistoryProcessInstanceStatus;
import java.io.IOException;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;

class HistoricProcessInstanceControllerIT extends BaseIT {

  @Test
  @Deployment(resources = "/bpmn/testHistoryProcessInstances.bpmn")
  void getHistoryProcessInstances() throws IOException {
    postForObject("api/process-definition/key/testHistoryProcessInstances/start", "", Map.class);

    var result = postForObject("api/extended/history/process-instance",
        "{\"rootProcessInstances\":true, \"finished\":true}", HistoryProcessInstanceDto[].class);

    assertThat(result).hasSize(1);
    assertThat(result[0])
        .hasFieldOrProperty("id")
        .hasFieldOrProperty("processDefinitionId")
        .hasFieldOrPropertyWithValue("processDefinitionName", "Test history process instances")
        .hasFieldOrProperty("startTime")
        .hasFieldOrProperty("endTime")
        .hasFieldOrPropertyWithValue("state", HistoryProcessInstanceStatus.COMPLETED)
        .hasFieldOrPropertyWithValue("processCompletionResult", "completion status")
        .hasFieldOrPropertyWithValue("excerptId", "excerpt id");
  }

  @Test
  @Deployment(resources = "/bpmn/testPendingProcessInstance.bpmn")
  void testPendingProcessInstance() throws IOException {
    postForObject("api/process-definition/key/testPendingProcessInstance/start", "", Map.class);

    var result = postForObject("api/extended/history/process-instance",
        "{\"rootProcessInstances\":true, \"unfinished\":true}", HistoryProcessInstanceDto[].class);

    assertThat(result).hasSize(1);
    assertThat(result[0])
        .hasFieldOrProperty("id")
        .hasFieldOrProperty("processDefinitionId")
        .hasFieldOrPropertyWithValue("processDefinitionName", "Test Pending Process Instance")
        .hasFieldOrProperty("startTime")
        .hasFieldOrProperty("endTime")
        .hasFieldOrPropertyWithValue("state", HistoryProcessInstanceStatus.ACTIVE)
        .hasFieldOrPropertyWithValue("processCompletionResult", null)
        .hasFieldOrPropertyWithValue("excerptId", null);
  }

  @Test
  @Deployment(resources = "/bpmn/testHistoryProcessInstances.bpmn")
  void getHistoryProcessInstanceById() throws IOException {
    var startResult = postForObject("api/process-definition/key/testHistoryProcessInstances/start",
        "", Map.class);
    var id = (String) startResult.get("id");

    var result = getForObject("api/extended/history/process-instance/" + id,
        HistoryProcessInstanceDto.class);

    assertThat(result)
        .hasFieldOrPropertyWithValue("id", id)
        .hasFieldOrProperty("processDefinitionId")
        .hasFieldOrPropertyWithValue("processDefinitionName", "Test history process instances")
        .hasFieldOrProperty("startTime")
        .hasFieldOrProperty("endTime")
        .hasFieldOrPropertyWithValue("state", HistoryProcessInstanceStatus.COMPLETED)
        .hasFieldOrPropertyWithValue("processCompletionResult", "completion status")
        .hasFieldOrPropertyWithValue("excerptId", "excerpt id");
  }

  @Test
  @Deployment(resources = "/bpmn/testPendingProcessInstance.bpmn")
  void getHistoryProcessInstanceById_active() throws IOException {
    var startResult = postForObject("api/process-definition/key/testPendingProcessInstance/start",
        "", Map.class);
    var id = (String) startResult.get("id");

    var result = getForObject("api/extended/history/process-instance/" + id,
        HistoryProcessInstanceDto.class);

    assertThat(result)
        .hasFieldOrPropertyWithValue("id", id)
        .hasFieldOrProperty("processDefinitionId")
        .hasFieldOrPropertyWithValue("processDefinitionName", "Test Pending Process Instance")
        .hasFieldOrProperty("startTime")
        .hasFieldOrProperty("endTime")
        .hasFieldOrPropertyWithValue("state", HistoryProcessInstanceStatus.ACTIVE)
        .hasFieldOrPropertyWithValue("processCompletionResult", null)
        .hasFieldOrPropertyWithValue("excerptId", null);
  }
}
