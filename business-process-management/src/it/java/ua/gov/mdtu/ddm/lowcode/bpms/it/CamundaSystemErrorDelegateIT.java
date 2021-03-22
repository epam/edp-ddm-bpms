package ua.gov.mdtu.ddm.lowcode.bpms.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.List;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import ua.gov.mdtu.ddm.general.errorhandling.exception.SystemException;

public class CamundaSystemErrorDelegateIT extends BaseIT {

  @Test
  @Deployment(resources = {"bpmn/testCamundaSystemErrorDelegate.bpmn"})
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
