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

import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.dto.FormDataInputWrapperDto;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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

    var formDataInputWrapper =
        FormDataInputWrapperDto.builder().key(storageKey).formData(formData).build();
    formDataStorageService.putFormData(formDataInputWrapper);

    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
    await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() ->
                assertThat(formDataStorageService.getFormData(storageKey)).isEmpty());
  }

  @Test
  @Deployment(resources = "bpmn/test_cleaner_on_start_form_exception.bpmn")
  void shouldDeleteStartFormDataOnStartFormExceptionTest() {
    var storageKey = "process-definition/processDefinitionId/start-form/randomUUID";
    var data = new LinkedHashMap<String, Object>();
    data.put("name", "TestName");
    var formData = FormDataDto.builder()
        .data(data)
        .build();
    Map<String, Object> vars = Map.of(StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME,
        storageKey);
    var formDataInputWrapper =
        FormDataInputWrapperDto.builder().key(storageKey).formData(formData).build();
    formDataStorageService.putFormData(formDataInputWrapper);

    var processInstance = runtimeService
        .startProcessInstanceByKey("startFormDataExceptionCleanerListenerKey", vars);

    BpmnAwareTests.assertThat(processInstance).isEnded();
    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() ->
            assertThat(formDataStorageService.getFormData(storageKey)).isEmpty());
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
    var formDataInputWrapper1 =
        FormDataInputWrapperDto.builder()
            .key(key)
            .formData(formData)
            .processInstanceId(processInstance.getId())
            .build();
    formDataStorageService.putFormData(formDataInputWrapper1);
    taskService.complete(firstTaskId);
    var secondTaskId = taskService.createTaskQuery().taskDefinitionKey(secondTaskDefinitionId)
        .singleResult().getId();
    var key2 = String.format("process/%s/task/%s", processInstance.getId(), secondTaskDefinitionId);
    var formDataInputWrapper2 =
            FormDataInputWrapperDto.builder()
                    .key(key2)
                    .formData(formData2)
                    .processInstanceId(processInstance.getId())
                    .build();
    formDataStorageService.putFormData(formDataInputWrapper2);

    Assertions.assertThat(formDataStorageService.getFormData(key)).isPresent();
    Assertions.assertThat(formDataStorageService.getFormData(key2)).isPresent();

    taskService.complete(secondTaskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              assertThat(formDataStorageService.getFormData(key)).isEmpty();
              assertThat(formDataStorageService.getFormData(key2)).isEmpty();
            });
  }

  @Test
  @Deployment(resources = "bpmn/test_cleaner_start_subprocess_by_error.bpmn")
  void shouldDeleteUserTaskFormDataWhenSubprocessStartedByErrorTest() {
    var firstTaskDefinitionId = "parent-process-task";
    var secondTaskDefinitionId = "child-process-task";
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
        .startProcessInstanceByKey("check-start-by-catch-error-event");
    var firstTaskId = taskService.createTaskQuery().taskDefinitionKey(firstTaskDefinitionId)
        .singleResult().getId();
    var key = String.format("process/%s/task/%s", processInstance.getId(), firstTaskDefinitionId);
    var formDataInputWrapper1 =
        FormDataInputWrapperDto.builder()
            .key(key)
            .formData(formData)
            .processInstanceId(processInstance.getId())
            .build();
    formDataStorageService.putFormData(formDataInputWrapper1);
    taskService.complete(firstTaskId);
    var secondTaskId = taskService.createTaskQuery().taskDefinitionKey(secondTaskDefinitionId)
        .singleResult().getId();
    var key2 = String.format("process/%s/task/%s", processInstance.getId(), secondTaskDefinitionId);
    var formDataInputWrapper2 =
        FormDataInputWrapperDto.builder()
            .key(key2)
            .formData(formData2)
            .processInstanceId(processInstance.getId())
            .build();
    formDataStorageService.putFormData(formDataInputWrapper2);

    Assertions.assertThat(formDataStorageService.getFormData(key)).isPresent();
    Assertions.assertThat(formDataStorageService.getFormData(key2)).isPresent();

    taskService.complete(secondTaskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              assertThat(formDataStorageService.getFormData(key)).isEmpty();
              assertThat(formDataStorageService.getFormData(key2)).isEmpty();
            });
  }

  @Test
  @Deployment(resources = "bpmn/testCleanSystemSignature.bpmn")
  void shouldDeleteSystemSignatures() {
    var processInstance = runtimeService
        .startProcessInstanceByKey("clean_system_signatures_key");
    var processInstanceId = processInstance.getId();

    var storageKey = formDataKeyProvider.generateSystemSignatureKey(processInstanceId,
        processInstanceId);
    var formDataInputWrapper =
        FormDataInputWrapperDto.builder()
            .key(storageKey)
            .formData(FormDataDto.builder().build())
            .processInstanceId(processInstanceId)
            .build();
    formDataStorageService.putFormData(formDataInputWrapper);
    assertThat(formDataStorageService.getFormData(storageKey)).isPresent();

    var taskId = taskService.createTaskQuery().taskDefinitionKey("Activity_1")
        .singleResult().getId();
    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(formDataStorageService.getFormData(storageKey)).isEmpty());
  }
}
