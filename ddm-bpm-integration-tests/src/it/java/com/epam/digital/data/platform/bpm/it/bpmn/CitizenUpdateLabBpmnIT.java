/*
 * Copyright 2023 EPAM Systems.
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

public class CitizenUpdateLabBpmnIT extends BaseBpmnIT {

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
  @Deployment(resources = {"bpmn/citizen-update-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void happyPathTest() throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    stubSearchSubjects("/xml/citizen-update-lab/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratoryToUpdate")
        .response("/json/citizen-update-lab/data-factory/laboratoryToUpdate.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("labToUpdateKoatuu")
        .response("/json/citizen-update-lab/data-factory/labToUpdateKoatuu.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu-equal-koatuu-id-name")
        .requestBody("{\"koatuuId\":\"labToUpdateKoatuu\"}")
        .response("/json/citizen-update-lab/data-factory/koatuuEqualKoatuuIdName.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("ownership")
        .resourceId("labToUpdateOwnership")
        .response("/json/citizen-update-lab/data-factory/labToUpdateOwnership.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("kopfg")
        .resourceId("labToUpdateKopfg")
        .response("/json/citizen-update-lab/data-factory/labToUpdateKopfg.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory-equal-edrpou-name-count")
        .requestBody("{\"edrpou\":\"01010101\",\"name\":\"updatedLabName\"}")
        .response("[]")
        .build());
    mockKeycloakGetUsersByRole("officer", "[]");
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testuser2Token))
        .requestBody("/json/citizen-update-lab/dso/dsoRequest_nameUnique.json")
        .response("{\"signature\": \"systemSignature\"}")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.PUT)
        .headers(Map.of("X-Access-Token", testuser2Token))
        .resource("laboratory")
        .resourceId("laboratoryToUpdate")
        .requestBody("/json/citizen-update-lab/data-factory/updatedLaboratoryRequest_name.json")
        .response("{}")
        .build());

    var startFormData = deserializeFormData("/json/citizen-update-lab/form-data/start_event.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId("citizen-update-lab",
        testUserToken, startFormData);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(processInstanceId)
        .activityDefinitionId("viewLabDataCitizenActivity")
        .formKey("read-lab-data-bp-view-lab-data")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json"))
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("viewLabDataCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(processInstanceId)
        .activityDefinitionId("updateLabCitizenActivity")
        .formKey("citizen-update-lab-bp-change-lab-data")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json"))
        .expectedVariables(Map.of("viewLabDataCitizenActivity_completer", "testuser",
            "laboratoryId", "laboratoryToUpdate"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("updateLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-update-lab/form-data/updateLabDataCitizenActivity_name.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signLabCitizenActivity")
        .formKey("shared-citizen-sign-lab")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/updateLabDataCitizenActivity_name.json"))
        .expectedVariables(Map.of("updateLabCitizenActivity_completer", "testuser"))
        .extensionElements(Map.of("eSign", "true", "ENTREPRENEUR", "true", "LEGAL", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-update-lab/form-data/signLabDataCitizenActivity_name.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(processInstanceId)
        .activityDefinitionId("dispatchTaskActivity")
        .formKey("shared-dispatch-task")
        .candidateRoles(List.of("task-dispatcher"))
        .extensionElements(Map.of("formVariables", "officerUsers"))
        .expectedVariables(Map.of("signLabCitizenActivity_completer", "testuser",
            "officerUsers", Collections.emptyList()))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("dispatchTaskActivity")
        .completerUserName(taskDispatcherUserName)
        .completerAccessToken(taskDispatcherToken)
        .expectedFormData("/json/citizen-update-lab/form-data/dispatchTaskActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(processInstanceId)
        .activityDefinitionId("checkLabOfficerActivity")
        .formKey("shared-officer-check-lab")
        .assignee(testuser2UserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/updateLabDataCitizenActivity_name.json"))
        .expectedVariables(Map.of("dispatchTaskActivity_completer", taskDispatcherUserName,
            "officerAssignee", testuser2UserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("checkLabOfficerActivity")
        .completerUserName(testuser2UserName)
        .completerAccessToken(testuser2Token)
        .expectedFormData(
            "/json/citizen-update-lab/form-data/checkLabDataOfficerActivity_nameUnique.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signLabOfficerActivity")
        .formKey("shared-officer-sign-lab")
        .assignee(testuser2UserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/checkLabDataOfficerActivity_nameUnique.json"))
        .expectedVariables(Map.of("checkLabOfficerActivity_completer", testuser2UserName))
        .extensionElements(Map.of("eSign", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signLabOfficerActivity")
        .completerUserName(testuser2UserName)
        .completerAccessToken(testuser2Token)
        .expectedFormData(
            "/json/citizen-update-lab/form-data/signLabDataOfficerActivity_nameUnique.json")
        .build());

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/citizen-update-lab/dso/dsoCephContent_nameUnique.json");

    addExpectedVariable("signLabOfficerActivity_completer", testuser2UserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Дані про лабораторію оновлені");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-update-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void labNotUniqueTest() throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    stubSearchSubjects("/xml/citizen-update-lab/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratoryToUpdate")
        .response("/json/citizen-update-lab/data-factory/laboratoryToUpdate.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("labToUpdateKoatuu")
        .response("/json/citizen-update-lab/data-factory/labToUpdateKoatuu.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu-equal-koatuu-id-name")
        .requestBody("{\"koatuuId\":\"labToUpdateKoatuu\"}")
        .response("/json/citizen-update-lab/data-factory/koatuuEqualKoatuuIdName.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("labToUpdateKoatuu")
        .response("/json/citizen-update-lab/data-factory/labToUpdateKoatuu.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("ownership")
        .resourceId("labToUpdateOwnership")
        .response("/json/citizen-update-lab/data-factory/labToUpdateOwnership.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("kopfg")
        .resourceId("labToUpdateKopfg")
        .response("/json/citizen-update-lab/data-factory/labToUpdateKopfg.json")
        .build());
    mockKeycloakGetUsersByRole("officer", "[]");

    var startFormData = deserializeFormData("/json/citizen-update-lab/form-data/start_event.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId("citizen-update-lab",
        testUserToken, startFormData);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(processInstanceId)
        .activityDefinitionId("viewLabDataCitizenActivity")
        .formKey("read-lab-data-bp-view-lab-data")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json"))
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("viewLabDataCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(processInstanceId)
        .activityDefinitionId("updateLabCitizenActivity")
        .formKey("citizen-update-lab-bp-change-lab-data")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json"))
        .expectedVariables(Map.of("viewLabDataCitizenActivity_completer", "testuser",
            "laboratoryId", "laboratoryToUpdate"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("updateLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-update-lab/form-data/updateLabDataCitizenActivity_accreditationFlag.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signLabCitizenActivity")
        .formKey("shared-citizen-sign-lab")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/updateLabDataCitizenActivity_accreditationFlag.json"))
        .expectedVariables(Map.of("updateLabCitizenActivity_completer", "testuser"))
        .extensionElements(Map.of("eSign", "true", "ENTREPRENEUR", "true", "LEGAL", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-update-lab/form-data/signLabDataCitizenActivity_accreditationFlag.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(processInstanceId)
        .activityDefinitionId("dispatchTaskActivity")
        .formKey("shared-dispatch-task")
        .candidateRoles(List.of("task-dispatcher"))
        .extensionElements(Map.of("formVariables", "officerUsers"))
        .expectedVariables(Map.of("signLabCitizenActivity_completer", "testuser",
            "officerUsers", Collections.emptyList()))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("dispatchTaskActivity")
        .completerUserName(taskDispatcherUserName)
        .completerAccessToken(taskDispatcherToken)
        .expectedFormData("/json/citizen-update-lab/form-data/dispatchTaskActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(processInstanceId)
        .activityDefinitionId("checkLabOfficerActivity")
        .formKey("shared-officer-check-lab")
        .assignee(testuser2UserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/updateLabDataCitizenActivity_accreditationFlag.json"))
        .expectedVariables(Map.of("dispatchTaskActivity_completer", taskDispatcherUserName,
            "officerAssignee", testuser2UserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("checkLabOfficerActivity")
        .completerUserName(testuser2UserName)
        .completerAccessToken(testuser2Token)
        .expectedFormData(
            "/json/citizen-update-lab/form-data/checkLabDataOfficerActivity_notUnique.json")
        .build());

    addExpectedVariable("checkLabOfficerActivity_completer", testuser2UserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Дані про лабораторію не оновлені - Така лабораторія вже існує");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-update-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void labDuplicateValidationException() throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    stubSearchSubjects("/xml/citizen-update-lab/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratoryToUpdate")
        .response("/json/citizen-update-lab/data-factory/laboratoryToUpdate.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("labToUpdateKoatuu")
        .response("/json/citizen-update-lab/data-factory/labToUpdateKoatuu.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu-equal-koatuu-id-name")
        .requestBody("{\"koatuuId\":\"labToUpdateKoatuu\"}")
        .response("/json/citizen-update-lab/data-factory/koatuuEqualKoatuuIdName.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("labToUpdateKoatuu")
        .response("/json/citizen-update-lab/data-factory/labToUpdateKoatuu.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("ownership")
        .resourceId("labToUpdateOwnership")
        .response("/json/citizen-update-lab/data-factory/labToUpdateOwnership.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("kopfg")
        .resourceId("labToUpdateKopfg")
        .response("/json/citizen-update-lab/data-factory/labToUpdateKopfg.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory-equal-edrpou-name-count")
        .requestBody("{\"edrpou\":\"01010101\",\"name\":\"updatedLabName\"}")
        .response("[{\"cnt\":1}]")
        .build());

    var startFormData = deserializeFormData("/json/citizen-update-lab/form-data/start_event.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId("citizen-update-lab",
        testUserToken, startFormData);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(processInstanceId)
        .activityDefinitionId("viewLabDataCitizenActivity")
        .formKey("read-lab-data-bp-view-lab-data")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json"))
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("viewLabDataCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(processInstanceId)
        .activityDefinitionId("updateLabCitizenActivity")
        .formKey("citizen-update-lab-bp-change-lab-data")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json"))
        .expectedVariables(Map.of("viewLabDataCitizenActivity_completer", "testuser",
            "laboratoryId", "laboratoryToUpdate"))
        .build());
    var result = completeTaskWithError(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("updateLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-update-lab/form-data/updateLabDataCitizenActivity_name.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(processInstanceId)
        .activityDefinitionId("updateLabCitizenActivity")
        .formKey("citizen-update-lab-bp-change-lab-data")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/updateLabDataCitizenActivity_name.json"))
        .build());

    var errors = result.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "name"),
        Map.entry("message", "Дані про цю лабораторію вже присутні"),
        Map.entry("value", "updatedLabName"));
  }

  @Test
  @SuppressWarnings("unchecked")
  @Deployment(resources = {"bpmn/citizen-update-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void subjectDisabledValidationException() throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    stubSearchSubjects("/xml/citizen-update-lab/searchSubjectsCancelledResponse.xml");

    var startFormData = deserializeFormData("/json/citizen-update-lab/form-data/start_event.json");
    var result = (Map<String, Map<String, List<Map<String, String>>>>) startProcessInstanceWithStartForm(
        "citizen-update-lab", testUserToken, startFormData);

    var errors = result.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", ""),
        Map.entry("message", "Суб'єкт скасовано або припинено"),
        Map.entry("value", ""));
  }
}
