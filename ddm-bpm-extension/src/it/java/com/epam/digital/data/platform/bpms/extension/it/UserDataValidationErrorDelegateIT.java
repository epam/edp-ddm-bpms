/*
 * Copyright 2023 EPAM Systems.
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

package com.epam.digital.data.platform.bpms.extension.it;

import static com.epam.digital.data.platform.bpms.extension.delegate.UserDataValidationUtils.VALIDATION_ERROR_MSG_PATTERN;
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
    var pdKey = "Process_0plk85h";
    ProcessInstance process = runtimeService
        .startProcessInstanceByKey(pdKey, "1", new HashMap<>());

    var tasks = engine.getTaskService().createTaskQuery().processInstanceId(process.getId()).list();
    var taskId = tasks.get(0).getId();
    var ex = assertThrows(ValidationException.class, () -> taskService.complete(taskId));

    var expectedExceptionMsg = String.format(
        VALIDATION_ERROR_MSG_PATTERN, pdKey, process.getId(),
        "well-readable-activity-id");
    assertThat(ex).isNotNull();
    assertThat(ex.getMessage()).isEqualTo(expectedExceptionMsg);
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
