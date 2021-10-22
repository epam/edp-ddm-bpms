package com.epam.digital.data.platform.bpms.extension.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import java.util.HashMap;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class UserDataValidationErrorDelegateIT extends BaseIT {

  @Test
  @Deployment(resources = {"bpmn/delegate/testBusinessValidationErrorDelegate.bpmn"})
  public void shouldThrowUserDataValidationException() {
    ProcessInstance process = runtimeService
        .startProcessInstanceByKey("Process_0plk85h", "1", new HashMap<>());

    var tasks = engine.getTaskService().createTaskQuery().processInstanceId(process.getId()).list();
    var taskId = tasks.get(0).getId();
    var ex = assertThrows(ValidationException.class, () -> taskService.complete(taskId));

    assertThat(ex).isNotNull();
    assertThat(ex.getDetails()).isNotNull();
    assertThat(ex.getDetails().getErrors()).isNotEmpty();
    var validationErrors = ex.getDetails().getErrors();
    assertThat(validationErrors).hasSize(2);

    var validationErrorDto1 = validationErrors.get(0);
    assertThat(validationErrorDto1.getMessage()).isEqualTo("test message");
    assertThat(validationErrorDto1.getField()).isEqualTo("taxPayerId");
    assertThat(validationErrorDto1.getValue()).isEqualTo("value");

    var validationErrorDto2 = validationErrors.get(1);
    assertThat(validationErrorDto2.getMessage()).isEqualTo("test message2");
    assertThat(validationErrorDto2.getField()).isEqualTo("taxPayerId2");
    assertThat(validationErrorDto2.getValue()).isEqualTo("value2");
  }
}
