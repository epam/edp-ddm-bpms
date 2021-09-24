package com.epam.digital.data.platform.bpms.it;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.epam.digital.data.platform.bpms.api.dto.HistoryUserTaskDto;
import java.util.Map;
import org.junit.Test;

public class HistoricTaskControllerIT extends BaseIT {

  @Test
  public void shouldGetHistoryTaskWithProcessDefinitionName() throws Exception {
    var taskDefinitionKey = "Activity_046yw3m";

    var startResult = postForObject(
        "api/process-definition/key/testGetExtendedTasks_key/start", "", Map.class);
    var processId = (String) startResult.get("id");

    var taskId = taskService.createTaskQuery().processInstanceId(processId)
        .taskDefinitionKey(taskDefinitionKey).singleResult()
        .getId();
    postForNoContent(String.format("api/task/%s/complete", taskId), "{}");

    var result = postForObject("api/extended/history/task",
        "{\"assignee\": \"testuser\", \"finished\": true}", HistoryUserTaskDto[].class);

    assertThat(result).isNotNull();
    assertThat(result[0].getAssignee()).isEqualTo("testuser");
    assertThat(result[0].getEndTime()).isNotNull();
    assertThat(result[0].getProcessDefinitionName()).isNotNull();
  }
}
