package com.epam.digital.data.platform.bpms.rest.it;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class TaskControllerIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/testGetExtendedTasks.bpmn")
  public void shouldGetTaskWithProcessDefinitionName() throws Exception {
    var startResult = postForObject("api/process-definition/key/testGetExtendedTasks_key/start", "",
        Map.class);
    var processId = (String) startResult.get("id");

    var result = postForObject("api/extended/task",
        "{\"assignee\": \"testuser\", \"processInstanceId\": \"" + processId + "\"}",
        UserTaskDto[].class);

    assertThat(result).isNotNull();
    assertThat(result[0].getAssignee()).isEqualTo("testuser");
    assertThat(result[0].getProcessDefinitionName()).isNotNull();
    assertThat(result[0].getProcessDefinitionName()).isEqualTo("Test Name");
  }
}
