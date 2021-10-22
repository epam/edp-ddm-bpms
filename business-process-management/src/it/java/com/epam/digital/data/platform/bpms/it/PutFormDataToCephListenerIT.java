package com.epam.digital.data.platform.bpms.it;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;

public class PutFormDataToCephListenerIT extends BaseIT {

  @Test
  @Deployment(resources = "/bpmn/testPutFormDataListener.bpmn")
  public void testPutFormDataToCephListener() {
    var processInstance = runtimeService.startProcessInstanceByKey("testPutFormDataListener");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("user_task");
    var cephKey = cephKeyProvider.generateKey("user_task", processInstance.getProcessInstanceId());
    var formData = cephService.getFormData(cephKey);
    assertThat(formData.get().getData()).hasSize(2).containsEntry("field1", "value1")
        .containsEntry("field2", "value2");
  }
}
