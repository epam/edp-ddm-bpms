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

package com.epam.digital.data.platform.bpms.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class JuelFunctionsIT extends BaseIT {

  @Inject
  protected HistoryService historyService;

  @Test
  @Deployment(resources = "bpmn/initiator_juel_function.bpmn")
  public void testInitiatorAccessToken() throws JsonProcessingException {
    var result = postForObject("api/process-definition/key/initiator_juel_function/start",
        "{}", Map.class);

    var vars = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId((String) result.get("id")).list();

    assertThat(vars).hasSize(2);
    var historicVarNames = vars.stream()
        .map(HistoricVariableInstance::getName)
        .collect(Collectors.toList());
    assertThat(historicVarNames)
        .hasSize(2)
        .contains("initiator", "elInitiator");
  }

  @Test
  @Deployment(resources = "bpmn/completer_juel_function.bpmn")
  public void testCompleterFunction() {
    var taskDefinitionKey = "waitConditionTaskKey";
    var processDefinitionKey = "testCompleterKey";
    var auth = new UsernamePasswordAuthenticationToken("testuser", null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    var processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey);

    formDataStorageService.putFormData(taskDefinitionKey, processInstance.getId(),
        FormDataDto.builder().accessToken(validAccessToken).build());

    String taskId = taskService.createTaskQuery().taskDefinitionKey(taskDefinitionKey)
        .singleResult().getId();
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
    Map<String, Object> vars = Map.of(StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME,
        "testKey");

    var processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, vars);

    formDataStorageService.putFormData(taskDefinitionKey, processInstance.getId(),
        FormDataDto.builder().data(formData).build());
    formDataStorageService.putFormData(startFormCephKey,
        FormDataDto.builder().data(formData).build());

    String taskId = taskService.createTaskQuery().taskDefinitionKey(taskDefinitionKey)
        .singleResult().getId();
    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "bpmn/sign_submission_juel_function.bpmn")
  public void testSignSubmissionFunction() {
    var startFormCephKey = "testKey";
    var taskDefinitionKey = "waitConditionSignTaskKey";
    var processDefinitionKey = "testSignSubmissionKey";
    var signature = "test signature";
    var data = new LinkedHashMap<String, Object>();
    data.put("userName", "testuser");
    Map<String, Object> vars = Map.of(StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME,
        "testKey");

    var processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, vars);

    formDataStorageService.putFormData(taskDefinitionKey, processInstance.getId(),
        FormDataDto.builder().data(data).signature(signature).build());
    formDataStorageService.putFormData(startFormCephKey,
        FormDataDto.builder().data(data).signature(signature).build());

    String taskId = taskService.createTaskQuery().taskDefinitionKey(taskDefinitionKey)
        .singleResult().getId();
    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "bpmn/system_user_juel_function.bpmn")
  public void testSystemUserFunction() {
    keycloakMockServer.resetAll();
    keycloakMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/auth/realms/system-user-realm/protocol/openid-connect/token"))
            .withRequestBody(equalTo("grant_type=client_credentials"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(convertJsonToString(
                    "/json/keycloak/keycloakSystemUserConnectResponse.json")))));

    var processInstance = runtimeService.startProcessInstanceByKey("test_system_user", Map.of());

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }
}
