package com.epam.digital.data.platform.bpms.rest.it;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.epam.digital.data.platform.bpms.api.dto.HistoryUserTaskDto;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class HistoricTaskControllerIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/testGetExtendedTasks.bpmn")
  public void shouldGetHistoryTaskWithProcessDefinitionName() throws Exception {
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
    assertThat(result.get(0).getProcessDefinitionName()).isNotNull();
  }
}
