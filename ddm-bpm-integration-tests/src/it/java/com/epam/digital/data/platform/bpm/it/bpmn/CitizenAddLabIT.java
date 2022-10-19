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

package com.epam.digital.data.platform.bpm.it.bpmn;

import static com.epam.digital.data.platform.bpm.it.util.CamundaAssertionUtil.processInstance;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpm.it.builder.StubData;
import com.epam.digital.data.platform.bpm.it.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpm.it.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpm.it.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpm.it.util.TestUtils;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CitizenAddLabIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_KEY = "citizen-add-lab";

  private final String taskDispatcherUserName = "taskdispatcher";
  private String taskDispatcherToken;

  private final String testuser2UserName = "testuser2";
  private String testuser2Token;

  @Before
  public void setUp() {
    taskDispatcherToken = TestUtils.getContent("/json/taskDispatcherAccessToken.json");
    testuser2Token = TestUtils.getContent("/json/testuser2AccessToken.json");
    mockConnectToKeycloak(officerRealm);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-add-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void happyPathTest() throws JsonProcessingException {
    stubSearchSubjects("/xml/citizen-add-lab/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory-equal-edrpou-name-count")
        .response("[]")
        .queryParams(Map.of("edrpou", "01010101", "name", "labName"))
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .response("/json/citizen-add-lab/data-factory/searchSubjectResponse.json")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .build());
    mockKeycloakGetUsersByRole("officer", "[]");
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testuser2Token))
        .requestBody("/json/citizen-add-lab/dso/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    var processInstanceId = startProcessInstance(PROCESS_DEFINITION_KEY, testUserToken);
    var processInstance = processInstance(processInstanceId);

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testuser2Token))
        .headers(Map.of("X-Digital-Signature",
            formDataKeyProvider.generateKey("signLabOfficerActivity", processInstanceId)))
        .resource("laboratory")
        .requestBody("/json/citizen-add-lab/data-factory/addLabRequestBody.json")
        .response("{}")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addLabCitizenActivity")
        .formKey("citizen-add-lab-bp-add-lab")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-lab/form-data/addLabCitizenActivityPrePopulation.json"))
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-add-lab/form-data/addLabCitizenActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signLabCitizenActivity")
        .formKey("shared-citizen-sign-lab")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-lab/form-data/signLabCitizenActivityPrePopulation.json"))
        .expectedVariables(Map.of("addLabCitizenActivity_completer", "testuser"))
        .extensionElements(Map.of("eSign", "true", "ENTREPRENEUR", "true", "LEGAL", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-add-lab/form-data/signLabCitizenActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("dispatchTaskActivity")
        .formKey("shared-dispatch-task")
        .candidateRoles(List.of("task-dispatcher"))
        .extensionElements(Map.of("formVariables", "officerUsers"))
        .expectedVariables(Map.of("signLabCitizenActivity_completer", "testuser",
            "officerUsers", Collections.emptyList(), "subjectId", "activeSubject"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("dispatchTaskActivity")
        .completerUserName(taskDispatcherUserName)
        .completerAccessToken(taskDispatcherToken)
        .expectedFormData("/json/citizen-add-lab/form-data/dispatchTaskActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("checkLabOfficerActivity")
        .formKey("shared-officer-check-lab")
        .assignee(testuser2UserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-lab/form-data/checkLabOfficerActivityPrePopulation.json"))
        .expectedVariables(Map.of("dispatchTaskActivity_completer", taskDispatcherUserName,
            "officerAssignee", testuser2UserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("checkLabOfficerActivity")
        .completerUserName(testuser2UserName)
        .completerAccessToken(testuser2Token)
        .expectedFormData("/json/citizen-add-lab/form-data/checkLabOfficerActivityUnique.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signLabOfficerActivity")
        .formKey("shared-officer-sign-lab")
        .assignee(testuser2UserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-lab/form-data/signLabOfficerActivityPrePopulation.json"))
        .expectedVariables(Map.of("checkLabOfficerActivity_completer", testuser2UserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signLabOfficerActivity")
        .completerUserName(testuser2UserName)
        .completerAccessToken(testuser2Token)
        .expectedFormData("/json/citizen-add-lab/form-data/signLabOfficerActivityUnique.json")
        .build());

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/citizen-add-lab/dso/digitalSignatureCephContent.json");

    addExpectedVariable("signLabOfficerActivity_completer", testuser2UserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT, "Лабораторія створена");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-add-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void duplicateLabCheckFailedTest() throws JsonProcessingException {
    stubSearchSubjects("/xml/citizen-add-lab/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory-equal-edrpou-name-count")
        .response("[]")
        .queryParams(Map.of("edrpou", "01010101", "name", "labName"))
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .response("/json/citizen-add-lab/data-factory/searchSubjectResponse.json")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .build());
    mockKeycloakGetUsersByRole("officer", "[]");

    var processInstanceId = startProcessInstance(PROCESS_DEFINITION_KEY, testUserToken);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addLabCitizenActivity")
        .formKey("citizen-add-lab-bp-add-lab")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-lab/form-data/addLabCitizenActivityPrePopulation.json"))
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-add-lab/form-data/addLabCitizenActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signLabCitizenActivity")
        .formKey("shared-citizen-sign-lab")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-lab/form-data/signLabCitizenActivityPrePopulation.json"))
        .expectedVariables(Map.of("addLabCitizenActivity_completer", "testuser"))
        .extensionElements(Map.of("eSign", "true", "ENTREPRENEUR", "true", "LEGAL", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-add-lab/form-data/signLabCitizenActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("dispatchTaskActivity")
        .formKey("shared-dispatch-task")
        .candidateRoles(List.of("task-dispatcher"))
        .extensionElements(Map.of("formVariables", "officerUsers"))
        .expectedVariables(Map.of("signLabCitizenActivity_completer", "testuser",
            "officerUsers", Collections.emptyList(), "subjectId", "activeSubject"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("dispatchTaskActivity")
        .completerUserName(taskDispatcherUserName)
        .completerAccessToken(taskDispatcherToken)
        .expectedFormData("/json/citizen-add-lab/form-data/dispatchTaskActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("checkLabOfficerActivity")
        .formKey("shared-officer-check-lab")
        .assignee(testuser2UserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-lab/form-data/checkLabOfficerActivityPrePopulation.json"))
        .expectedVariables(Map.of("dispatchTaskActivity_completer", taskDispatcherUserName,
            "officerAssignee", testuser2UserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .activityDefinitionId("checkLabOfficerActivity")
        .completerUserName(testuser2UserName)
        .completerAccessToken(testuser2Token)
        .expectedFormData("/json/citizen-add-lab/form-data/checkLabOfficerActivityDuplicate.json")
        .build());

    addExpectedVariable("checkLabOfficerActivity_completer", testuser2UserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Лабораторія не створена - Така лабораторія вже існує");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-add-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void duplicateLabTest() throws JsonProcessingException {
    stubSearchSubjects("/xml/citizen-add-lab/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory-equal-edrpou-name-count")
        .response("[{\"cnt\":1}]")
        .queryParams(Map.of("edrpou", "01010101", "name", "labName"))
        .build());

    var processInstanceId = startProcessInstance("citizen-add-lab", testUserToken);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addLabCitizenActivity")
        .formKey("citizen-add-lab-bp-add-lab")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-lab/form-data/addLabCitizenActivityPrePopulation.json"))
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());
    var result = completeTaskWithError(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-add-lab/form-data/addLabCitizenActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addLabCitizenActivity")
        .formKey("citizen-add-lab-bp-add-lab")
        .assignee("testuser")
        .expectedFormDataPrePopulation(
            deserializeFormData("/json/citizen-add-lab/form-data/addLabCitizenActivity.json"))
        .build());

    var errors = result.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "name"),
        Map.entry("message", "Дані про цю лабораторію вже присутні"),
        Map.entry("value", "labName"));
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-add-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void disabledSubjectTest() throws JsonProcessingException {
    stubSearchSubjects("/xml/citizen-add-lab/searchSubjectsCancelledResponse.xml");

    var result = startProcessInstanceWithError(PROCESS_DEFINITION_KEY, testUserToken);

    var errors = result.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", ""),
        Map.entry("message", "Суб'єкт скасовано або припинено"),
        Map.entry("value", ""));
  }
}
