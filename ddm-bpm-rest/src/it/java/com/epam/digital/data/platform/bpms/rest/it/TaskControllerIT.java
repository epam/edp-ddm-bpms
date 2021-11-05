package com.epam.digital.data.platform.bpms.rest.it;


import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.bpms.api.dto.SignableUserTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import com.epam.digital.data.platform.dso.api.dto.Subject;
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

  @Test
  public void shouldGetTaskWithTaskProperties() throws Exception {
    var startResult = postForObject("api/process-definition/key/testGetExtendedTasks_key/start", "",
        Map.class);
    var processId = (String) startResult.get("id");
    var task = taskService.createTaskQuery().processInstanceId(processId).list().get(0);
    var taskId = task.getId();

    var result = getForObject("api/extended/task/" + taskId, SignableUserTaskDto.class);

    assertThat(result.getProcessDefinitionName()).isEqualTo("Test Name");
    assertThat(result.getFormVariables()).hasSize(2)
        .containsAllEntriesOf(Map.of("formVariable", "var1", "formVariable2", "var2"));
    assertThat(result.getSignatureValidationPack()).hasSize(1).contains(Subject.ENTREPRENEUR);
    assertThat(result.isESign()).isTrue();
  }
}
