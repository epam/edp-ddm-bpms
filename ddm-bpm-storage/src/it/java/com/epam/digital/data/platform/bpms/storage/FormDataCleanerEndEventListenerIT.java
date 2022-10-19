/*
 * Copyright 2021 EPAM Systems.
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

package com.epam.digital.data.platform.bpms.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import java.util.LinkedHashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.jupiter.api.Test;

class FormDataCleanerEndEventListenerIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/start_form_data_cleaner_listener.bpmn")
  void shouldDeleteStartFormDataTest() {
    var storageKey = "process-definition/processDefinitionId/start-form/randomUUID";
    var data = new LinkedHashMap<String, Object>();
    data.put("name", "TestName");
    var formData = FormDataDto.builder()
        .data(data)
        .build();
    Map<String, Object> vars = Map.of(StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME,
        storageKey);

    var processInstance = runtimeService
        .startProcessInstanceByKey("startFormDataCleanerListenerKey", vars);
    var taskId = taskService.createTaskQuery().taskDefinitionKey("startFormDataCleanerListenerId")
        .singleResult().getId();

    formDataStorageService.putFormData(storageKey, formData);

    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
    assertThat(formDataStorageService.getFormData(storageKey)).isEmpty();
  }

  @Test
  @Deployment(resources = "bpmn/user_task_form_data_cleaner_listener.bpmn")
  void shouldDeleteUserTaskFormDataTest() {
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
    formDataStorageService.putFormData(key, formData);
    taskService.complete(firstTaskId);
    var secondTaskId = taskService.createTaskQuery().taskDefinitionKey(secondTaskDefinitionId)
        .singleResult().getId();
    var key2 = String.format("process/%s/task/%s", processInstance.getId(), secondTaskDefinitionId);
    formDataStorageService.putFormData(key2, formData2);

    Assertions.assertThat(formDataStorageService.getFormData(key)).isPresent();
    Assertions.assertThat(formDataStorageService.getFormData(key2)).isPresent();

    taskService.complete(secondTaskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
    assertThat(formDataStorageService.getFormData(key)).isEmpty();
    assertThat(formDataStorageService.getFormData(key2)).isEmpty();
  }

  @Test
  @Deployment(resources = "bpmn/testCleanSystemSignature.bpmn")
  void shouldDeleteSystemSignatures() {
    var processInstance = runtimeService
        .startProcessInstanceByKey("clean_system_signatures_key");
    var processInstanceId = processInstance.getId();

    var storageKey = formDataKeyProvider.generateSystemSignatureKey(processInstanceId,
        processInstanceId);
    formDataStorageService.putFormData(storageKey, FormDataDto.builder().build());
    assertThat(formDataStorageService.getFormData(storageKey)).isPresent();

    var taskId = taskService.createTaskQuery().taskDefinitionKey("Activity_1")
        .singleResult().getId();
    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
    assertThat(formDataStorageService.getFormData(storageKey)).isEmpty();
  }
}
