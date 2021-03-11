package ua.gov.mdtu.ddm.lowcode.bpms.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.HashMap;
import java.util.List;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.UserDataValidationErrorDto;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ValidationErrorDto;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.UserDataValidationException;

public class UserDataValidationErrorDelegateIT extends BaseIT {

  @Test
  @Deployment(resources = {"bpmn/testBusinessValidationErrorDelegate.bpmn"})
  public void shouldThrowUserDataValidationException() {
    ProcessInstance process = runtimeService
        .startProcessInstanceByKey("Process_0plk85h", "1", new HashMap<>());

    List<Task> tasks = engine.getTaskService().createTaskQuery().processInstanceId(process.getId()).list();
    UserDataValidationException ex = assertThrows(UserDataValidationException.class,
        () -> taskService.complete(tasks.get(0).getId()));

    assertThat(ex.getErrorDto()).isNotNull();
    UserDataValidationErrorDto errorDto = ex.getErrorDto();
    assertThat(errorDto.getDetails()).isNotNull();
    assertThat(errorDto.getDetails().getErrors()).isNotEmpty();
    List<ValidationErrorDto> validationErrors = errorDto.getDetails().getErrors();
    assertThat(validationErrors).hasSize(2);

    ValidationErrorDto validationErrorDto1 = validationErrors.get(0);
    assertThat(validationErrorDto1.getMessage()).isEqualTo("test message");
    assertThat(validationErrorDto1.getField()).isEqualTo("taxPayerId");
    assertThat(validationErrorDto1.getValue()).isEqualTo("value");

    ValidationErrorDto validationErrorDto2 = validationErrors.get(1);
    assertThat(validationErrorDto2.getMessage()).isEqualTo("test message2");
    assertThat(validationErrorDto2.getField()).isEqualTo("taxPayerId2");
    assertThat(validationErrorDto2.getValue()).isEqualTo("value2");
  }
}
