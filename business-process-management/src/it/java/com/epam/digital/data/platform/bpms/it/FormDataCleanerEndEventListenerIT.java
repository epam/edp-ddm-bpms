package com.epam.digital.data.platform.bpms.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import java.util.LinkedHashMap;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;

public class FormDataCleanerEndEventListenerIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/start_form_data_cleaner_listener.bpmn")
  public void shouldDeleteStartFormDataTest() {
    var cephKey = "process-definition/processDefinitionId/start-form/randomUUID";
    var data = new LinkedHashMap<String, Object>();
    data.put("name", "TestName");
    var formData = FormDataDto.builder()
        .data(data)
        .build();
    Map<String, Object> vars = Map.of("start_form_ceph_key", cephKey);

    var processInstance = runtimeService
        .startProcessInstanceByKey("startFormDataCleanerListenerKey", vars);
    var taskId = taskService.createTaskQuery().taskDefinitionKey("startFormDataCleanerListenerId")
        .singleResult().getId();

    cephService.putFormData(cephKey, formData);

    assertThat(cephService.getStorage()).hasSize(1);

    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
    assertThat(cephService.getStorage()).isEmpty();
  }

  @Test
  @Deployment(resources = "bpmn/user_task_form_data_cleaner_listener.bpmn")
  public void shouldDeleteUserTaskFormDataTest() {
    var firstTaskDefinitionId = "userTaskFormDataCleanerListenerId1";
    var secondTaskDefinitionId = "userTaskFormDataCleanerListenerId2";
    var data = new LinkedHashMap<String, Object>();
    data.put("name", "TestName");
    var formData = FormDataDto.builder()
        .data(data)
        .build();
    var data2 = new LinkedHashMap<String, Object>();
    data2.put("fullName", "Test Full Name");
    var formData2 = FormDataDto.builder()
        .data(data2)
        .build();

    var processInstance = runtimeService
        .startProcessInstanceByKey("userTaskFormDataCleanerListenerKey");
    var firstTaskId = taskService.createTaskQuery().taskDefinitionKey(firstTaskDefinitionId)
        .singleResult().getId();
    var key = String.format("process/%s/task/%s", processInstance.getId(), firstTaskDefinitionId);
    cephService.putFormData(key, formData);
    taskService.complete(firstTaskId);
    var secondTaskId = taskService.createTaskQuery().taskDefinitionKey(secondTaskDefinitionId)
        .singleResult().getId();
    var key2 = String.format("process/%s/task/%s", processInstance.getId(), secondTaskDefinitionId);
    cephService.putFormData(key2, formData2);

    assertThat(cephService.getStorage()).hasSize(2);

    taskService.complete(secondTaskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
    assertThat(cephService.getStorage()).isEmpty();
  }
}
