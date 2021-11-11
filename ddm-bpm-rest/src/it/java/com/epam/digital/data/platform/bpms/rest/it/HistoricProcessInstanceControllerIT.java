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
}
