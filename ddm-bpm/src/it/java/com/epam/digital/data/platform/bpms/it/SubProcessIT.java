package com.epam.digital.data.platform.bpms.it;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;

public class SubProcessIT extends BaseIT {

  @Test
  @Deployment(resources = "/bpmn/testSubProcess.bpmn")
  public void testSubProcess() {
    var processInstance = runtimeService.startProcessInstanceByKey("testSubProcessKey");

    var rootUserTaskId = taskService.createTaskQuery()
        .taskDefinitionKey("rootUserTaskProcessActivity").singleResult().getId();
    taskService.complete(rootUserTaskId);

    var subProcessUserTaskId = taskService.createTaskQuery()
        .taskDefinitionKey("subProcessUserTaskActivity").singleResult().getId();
    taskService.complete(subProcessUserTaskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }
}
