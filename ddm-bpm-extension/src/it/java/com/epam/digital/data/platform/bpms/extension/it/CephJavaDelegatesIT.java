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

package com.epam.digital.data.platform.bpms.extension.it;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.google.common.collect.ImmutableMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.spin.Spin;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

public class CephJavaDelegatesIT extends BaseIT {

  @Value("${ceph.bucket}")
  private String cephBucketName;

  @Before
  public void setUp() {
    cephService.setCephBucketName(cephBucketName);
  }

  @Test
  @Deployment(resources = {"bpmn/delegate/testCephJavaDelegates.bpmn"})
  public void shouldUseCephJavaDelegatesInServiceTasks() {
    String contentToPut = "{\"data\":{\"var1\":\"value1\",\"var2\":\"value2\"}}";

    Map<String, Object> vars = ImmutableMap.of(
        "key", "testKey",
        "content", Variables.stringValue(contentToPut, true));
    ProcessInstance process = runtimeService
        .startProcessInstanceByKey("testCephJavaDelegates_key", "1", vars);

    assertTrue(process.isEnded());

    var content = formDataStorageService.getFormData("testKey").get();
    assertThat(content).isNotNull()
        .isEqualTo(deserializeFormData(contentToPut));
  }

  @Test
  @Deployment(resources = {"bpmn/delegate/testCephFormDataDelegates.bpmn"})
  public void shouldPutTaskFormDataToCeph() {
    var content = Spin.JSON("{\"name\":\"value ek\"}");

    Map<String, Object> vars = ImmutableMap.of("formData", Variables.objectValue(content, true));
    var processInstance = runtimeService
        .startProcessInstanceByKey("testCephFormDataDelegates_key", "key", vars);

    var expectedCephKey = formDataKeyProvider.generateKey("userTask",
        processInstance.getProcessInstanceId());

    var data = formDataStorageService.getFormData(expectedCephKey);
    assertThat(data).isNotEmpty();
    assertThat(data.get().getData().get("name")).isEqualTo("value ek");

    var taskId = taskService.createTaskQuery().taskDefinitionKey("waitCheckPutFormData")
        .singleResult().getId();
    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();

    var resultVariables = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getId()).list().stream()
        .filter(historicVariableInstance -> Objects.nonNull(historicVariableInstance.getValue()))
        .collect(toMap(HistoricVariableInstance::getName, HistoricVariableInstance::getValue,
            (o1, o2) -> o1));

    assertThat(resultVariables).doesNotContainKey("formDataOutput");
  }

  @Test
  @Deployment(resources = {"bpmn/delegate/testDefineProcessStatus.bpmn"})
  public void shouldSaveProcessStatusAsSysVariable() {
    var status = "awesome";
    Map<String, Object> vars = ImmutableMap.of("status", status);
    var processInstance = runtimeService
        .startProcessInstanceByKey("testDefineProcessStatus_key", "key", vars);

    assertTrue(processInstance.isEnded());

    var resultVariables = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getId()).list().stream()
        .collect(toMap(HistoricVariableInstance::getName, HistoricVariableInstance::getValue,
            (o1, o2) -> o1));
    assertThat(resultVariables).containsEntry(
        ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT, status);
  }

  @Test
  @Deployment(resources = {"bpmn/delegate/testStartFormKey.bpmn"})
  public void shouldReadStartFormData() {
    Map<String, Object> vars = Map.of(
        StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, "cephKey");

    var data = new LinkedHashMap<String, Object>();
    data.put("prop1", "value1");

    formDataStorageService.putFormData("cephKey", FormDataDto.builder().data(data).build());

    var processInstance = runtimeService
        .startProcessInstanceByKey("testStartFormKey", "key", vars);

    assertTrue(processInstance.isEnded());
  }
}
