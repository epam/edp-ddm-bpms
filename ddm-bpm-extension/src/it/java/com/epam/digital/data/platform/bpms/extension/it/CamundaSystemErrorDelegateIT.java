package com.epam.digital.data.platform.bpms.extension.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.epam.digital.data.platform.starter.errorhandling.exception.SystemException;
import java.util.List;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class CamundaSystemErrorDelegateIT extends BaseIT {

  @Test
  @Deployment(resources = {"bpmn/delegate/testCamundaSystemErrorDelegate.bpmn"})
  public void shouldThrowCamundaSystemException() {
    ProcessInstance process = runtimeService
        .startProcessInstanceByKey("testCamundaSystemErrorDelegate_key");

    List<Task> tasks = engine.getTaskService().createTaskQuery().processInstanceId(process.getId())
        .list();

    var id = tasks.get(0).getId();
    var ex = assertThrows(SystemException.class, () -> taskService.complete(id));

    assertThat(ex.getMessage()).isEqualTo("System error");
    assertThat(ex.getLocalizedMessage()).isEqualTo("Something wrong");
  }
}
