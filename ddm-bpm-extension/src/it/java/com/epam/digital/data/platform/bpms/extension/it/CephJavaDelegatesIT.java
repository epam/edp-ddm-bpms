package com.epam.digital.data.platform.bpms.extension.it;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.exception.MisconfigurationException;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
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
  public void shouldThrowAnExceptionIfBucketNotExists() {
    String contentToPut = "{ \"var1\":\"value1\", \"var2\":\"value2\" }";
    Map<String, Object> vars = new HashMap<>();
    vars.put("key", "testKey");
    vars.put("content", Variables.stringValue(contentToPut, true));

    this.cephService.setCephBucketName("newName");
    var ex = assertThrows(MisconfigurationException.class, () -> runtimeService
        .startProcessInstanceByKey("testCephJavaDelegates_key", "1", vars));

    assertThat(ex.getMessage()).isEqualTo("Bucket bucket hasn't found");
  }

  @Test
  @Deployment(resources = {"bpmn/delegate/testCephJavaDelegates.bpmn"})
  public void shouldUseCephJavaDelegatesInServiceTasks() {
    String contentToPut = "{ \"var1\":\"value1\", \"var2\":\"value2\" }";

    Map<String, Object> vars = ImmutableMap.of(
        "key", "testKey",
        "content", Variables.stringValue(contentToPut, true));
    ProcessInstance process = runtimeService
        .startProcessInstanceByKey("testCephJavaDelegates_key", "1", vars);

    assertTrue(process.isEnded());

    String content = cephService.getContent(cephBucketName, "testKey").get();
    assertThat(content).isNotNull()
        .isEqualTo(contentToPut);
  }

  @Test
  @Deployment(resources = {"bpmn/delegate/testCephFormDataDelegates.bpmn"})
  public void shouldPutTaskFormDataToCeph() {
    var content = Spin.JSON("{\"name\":\"value ek\"}");

    Map<String, Object> vars = ImmutableMap.of("formData", Variables.objectValue(content, true));
    var processInstance = runtimeService
        .startProcessInstanceByKey("testCephFormDataDelegates_key", "key", vars);

    var expectedCephKey = cephKeyProvider.generateKey("userTask", processInstance.getProcessInstanceId());

    var data = cephService.getFormData(expectedCephKey);
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
    assertThat(resultVariables).containsEntry("sys-var-process-completion-result", status);
  }

  @Test
  @Deployment(resources = {"bpmn/delegate/testStartFormKey.bpmn"})
  public void shouldReadStartFormData() {
    Map<String, Object> vars = ImmutableMap.of(
        Constants.BPMS_START_FORM_CEPH_KEY_VARIABLE_NAME, "cephKey");

    var data = new LinkedHashMap<String, Object>();
    data.put("prop1", "value1");

    cephService.putFormData("cephKey", FormDataDto.builder().data(data).build());

    var processInstance = runtimeService
        .startProcessInstanceByKey("testStartFormKey", "key", vars);

    assertTrue(processInstance.isEnded());
  }
}
