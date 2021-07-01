package com.epam.digital.data.platform.bpms.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;

public class JuelFunctionsIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/initiator_juel_function.bpmn")
  public void testInitiatorAccessToken() throws JsonProcessingException {
    var result = postForObject("api/process-definition/key/initiator_juel_function/start",
        "{}", Map.class);

    var vars = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId((String) result.get("id")).list();

    assertThat(vars).hasSize(3);
    var historicVarNames = vars.stream()
        .map(HistoricVariableInstance::getName)
        .collect(Collectors.toList());
    assertThat(historicVarNames)
        .hasSize(3)
        .contains("initiator", "const_dataFactoryBaseUrl", "elInitiator");
  }

  @Test
  @Deployment(resources = "bpmn/completer_juel_function.bpmn")
  public void testCompleterFunction() {
    var taskDefinitionKey = "waitConditionTaskKey";
    var processDefinitionKey = "testCompleterKey";

    var processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey);

    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstance.getId());
    cephService.putFormData(cephKey, FormDataDto.builder().accessToken(validAccessToken).build());

    String taskId = taskService.createTaskQuery().taskDefinitionKey(taskDefinitionKey).singleResult().getId();
    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "bpmn/submission_juel_function.bpmn")
  public void testSubmissionFunction() {
    var startFormCephKey = "testKey";
    var taskDefinitionKey = "waitConditionTaskKey";
    var processDefinitionKey = "testSubmissionKey";
    var formData = new LinkedHashMap<String, Object>();
    formData.put("userName", "testuser");
    Map<String, Object> vars = Map.of("start_form_ceph_key", "testKey");

    var processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, vars);

    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstance.getId());
    cephService.putFormData(cephKey, FormDataDto.builder().data(formData).build());
    cephService.putFormData(startFormCephKey, FormDataDto.builder().data(formData).build());

    String taskId = taskService.createTaskQuery().taskDefinitionKey(taskDefinitionKey).singleResult().getId();
    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }
}
