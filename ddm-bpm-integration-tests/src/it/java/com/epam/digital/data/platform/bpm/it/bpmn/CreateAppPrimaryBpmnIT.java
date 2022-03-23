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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.historyService;

import com.epam.digital.data.platform.bpm.it.builder.StubData;
import com.epam.digital.data.platform.bpm.it.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpm.it.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpm.it.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import java.io.IOException;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CreateAppPrimaryBpmnIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_KEY = "create-app-primary";

  @Test
  @Deployment(resources = {"bpmn/create-app-primary.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testAdditionHappyPass_accreditationFlagIsTrue() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";
    stubSearchSubjects("/xml/create-app/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/create-app/data-factory/last-laboratory-solution-deny.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/solutionTypeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/applicationTypeResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app/data-factory/findLaboratoryResponse.json")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-app/dso/primaryIncludeSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("registration")
        .requestBody("/json/create-app/data-factory/createApplicationIncludeRequest.json")
        .response("{}")
        .build());

    var data = deserializeFormData("/json/create-app/form-data/start_event.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        testUserToken, data);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-application")
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-edrpou-prepopulation.json"))
        .expectedVariables(Map.of("initiator", testUserName,
            StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY,
            "fullName", "testuser testuser testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-application")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app/form-data/Activity_shared-add-application.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-factors")
        .formKey("shared-add-factors")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-add-application_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-factors")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app/form-data/Activity_shared-add-factors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-check-complience")
        .formKey("shared-check-complience")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-add-application_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-check-complience")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-check-compliance_noErrors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-decision-include")
        .formKey("shared-add-decision-include")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-solution-include-prepopulation.json"))
        .expectedVariables(
            Map.of("Activity_shared-check-complience_completer", testUserName, "solutionTypeId",
                "ADD=SUCCESS"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-decision-include")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-add-decision-include.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-letter-data")
        .formKey("shared-add-letter-data")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-solution-include-prepopulation.json"))
        .expectedVariables(
            Map.of("Activity_shared-add-decision-include_completer", testUserName, "solutionTypeId",
                "ADD=SUCCESS"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-letter-data")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-add-letter-data.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-sign-app-include")
        .formKey("shared-sign-app-include")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/Activity_shared-sign-app-include_prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-add-letter-data_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-sign-app-include")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-sign-app-include.json")
        .build());

    addExpectedVariable("Activity_shared-sign-app-include_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Прийнято рішення про внесення лабораторії до переліку");

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/create-app/dso/primaryIncludeSystemSignatureCephContent.json");
    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/create-app-primary.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testDenyingHappyPass_accreditationFlagIsTrue() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";
    stubSearchSubjects("/xml/create-app/searchSubjectsDisabledResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app/data-factory/findLaboratoryResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "WO_CONSIDER"))
        .response("/json/create-app/data-factory/solutionTypeWoConsiderResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/applicationTypeResponse.json")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-app/dso/primaryDenySystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("registration")
        .requestBody("/json/create-app/data-factory/createApplicationPrimaryDenyRequest.json")
        .response("{}")
        .build());

    var data = deserializeFormData("/json/create-app/form-data/start_event.json");
    data.setAccessToken(testUserToken);
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        testUserToken, data);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-subject-status-error")
        .formKey("shared-subject-status-error")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName,
            StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-subject-status-error")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app/form-data/Activity_shared-subject-status-error.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-application")
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-edrpou-prepopulation.json"))
        .expectedVariables(Map.of("edrSuspendedOrCancelled", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-application")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app/form-data/Activity_shared-add-application.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-factors")
        .formKey("shared-add-factors")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-add-application_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-factors")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app/form-data/Activity_shared-add-factors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-check-complience")
        .formKey("shared-check-complience")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-add-application_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-check-complience")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-check-compliance_errors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-decision-deny")
        .formKey("shared-add-decision-deny")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-solution-deny-prepopulation.json"))
        .expectedVariables(
            Map.of("Activity_shared-check-complience_completer", testUserName, "solutionTypeId",
                "woconsider"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-decision-deny")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-add-decision-deny.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_1eujure")
        .formKey("shared-add-letter-data")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-solution-deny-prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-add-decision-deny_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_1eujure")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-add-letter-data.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-sign-app-deny")
        .formKey("shared-sign-app-deny")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/Activity_shared-sign-app-deny_prepopulation.json"))
        .expectedVariables(Map.of("Activity_1eujure_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-sign-app-deny")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-sign-app-deny.json")
        .build());

    addExpectedVariable("fullName", "testuser testuser testuser");
    addExpectedVariable("Activity_shared-sign-app-deny_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Прийнято рішення про залишення без розгляду");

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);

    addExpectedVariable("x_digital_signature_derived_ceph_key", systemSignatureCephKey);

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/create-app/dso/primaryDenySystemSignatureCephContent.json");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = "bpmn/create-app-primary.bpmn")
  public void testValidationError() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";
    stubSearchSubjects("/xml/create-app/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/create-app/data-factory/last-laboratory-solution-deny.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/solutionTypeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/applicationTypeResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app/data-factory/findLaboratoryWithoutAccreditationResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-equal-laboratory-id-count")
        .queryParams(Map.of("laboratoryId", labId))
        .response("[]")
        .build());

    var data = deserializeFormData("/json/create-app/form-data/start_event.json");
    var resultMap = startProcessInstanceWithStartFormForError(PROCESS_DEFINITION_KEY, testUserToken,
        data);

    var errors = resultMap.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "laboratory"),
        Map.entry("message", "Додайте кадровий склад до лабораторії"),
        Map.entry("value", labId));
  }

  @Test
  @Deployment(resources = "bpmn/create-app-primary.bpmn")
  public void testNoAccreditationFlag() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";
    stubSearchSubjects("/xml/create-app/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/create-app/data-factory/last-laboratory-solution-deny.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/solutionTypeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/applicationTypeResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response(
            "/json/create-app/data-factory/findLaboratoryWithoutAccreditationResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-equal-laboratory-id-count")
        .queryParams(Map.of("laboratoryId", labId))
        .response("[{\"cnt\":1}]")
        .build());

    var data = deserializeFormData("/json/create-app/form-data/start_event.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        testUserToken, data);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-application")
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-edrpou-prepopulation.json"))
        .expectedVariables(Map.of("initiator", testUserName,
            StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY,
            "fullName", "testuser testuser testuser"))
        .build());
  }

  @Test
  @Deployment(resources = "bpmn/create-app-primary.bpmn")
  public void testAppAlreadyCreated() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";
    stubSearchSubjects("/xml/create-app/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/create-app/data-factory/last-laboratory-solution-add.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/solutionTypeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/applicationTypeResponse.json")
        .build());

    var data = deserializeFormData("/json/create-app/form-data/start_event.json");
    var resultMap = startProcessInstanceWithStartFormForError(PROCESS_DEFINITION_KEY, testUserToken,
        data);

    var errors = resultMap.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "laboratory"),
        Map.entry("message", "Заява на первинне внесення вже створена"),
        Map.entry("value", labId));
  }

  @Test
  @Deployment(resources = "bpmn/create-app-primary.bpmn")
  public void testSubjectIsDisabledButNoErrors() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";
    stubSearchSubjects("/xml/create-app/searchSubjectsDisabledResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app/data-factory/findLaboratoryResponse.json")
        .build());

    var data = deserializeFormData("/json/create-app/form-data/start_event.json");
    data.setAccessToken(testUserToken);
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        testUserToken, data);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-subject-status-error")
        .formKey("shared-subject-status-error")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName,
            StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-subject-status-error")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-subject-status-error.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-application")
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-edrpou-prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-subject-status-error_completer", testUserName,
            "edrSuspendedOrCancelled", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-application")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app/form-data/Activity_shared-add-application.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-factors")
        .formKey("shared-add-factors")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-add-application_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-add-factors")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app/form-data/Activity_shared-add-factors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-check-complience")
        .formKey("shared-check-complience")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-add-factors_completer", testUserName))
        .build());
    var result = completeTaskWithError(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_shared-check-complience")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-check-compliance_noErrors.json")
        .build());

    var errors = result.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "errorCheckFlag"),
        Map.entry("message",
            "Статус суб'єкта в ЄДР \"Скаcовано\" або \"Припинено\", оберіть відповідну причину відмови"),
        Map.entry("value", "false"));
  }
}
