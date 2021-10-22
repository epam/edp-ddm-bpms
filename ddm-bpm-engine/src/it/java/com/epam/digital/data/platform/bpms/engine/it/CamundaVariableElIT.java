package com.epam.digital.data.platform.bpms.engine.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class CamundaVariableElIT extends BaseIT {

  @Test
  @Deployment(resources = {"bpmn/testNonTransientBooleanExpressionLanguage.bpmn"})
  public void shouldThrowAnExceptionIfBucketNotExists() {
    var processInstance = runtimeService
        .startProcessInstanceByKey("testNonTransientBooleanExpressionLanguage_key");

    assertFalse(processInstance.isEnded());
    var tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getTaskDefinitionKey()).isEqualTo("Activity_0hdcjra");
  }
}
