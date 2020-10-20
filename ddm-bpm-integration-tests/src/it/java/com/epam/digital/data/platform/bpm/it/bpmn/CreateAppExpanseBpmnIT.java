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

public class CreateAppExpanseBpmnIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_KEY = "create-app-expanse";

  @Test
  @Deployment(resources = {"bpmn/create-app-expanse.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testAdditionHappyPass_accreditationFlagIsTrue() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    var addApplicationActivityDefinitionKey = "Activity_shared-add-application";
    var addFactorsActivityDefinitionKey = "Activity_shared-add-factors";
    var checkComplianceActivityDefinitionKey = "Activity_shared-check-complience";
    var addDecisionIncludeActivityDefinitionKey = "Activity_shared-add-decision-include";
    var addLetterDataActivityDefinitionKey = "Activity_shared-add-letter-data";
    var signAppIncludeActivityDefinitionKey = "Activity_shared-sign-app-include";

    stubSearchSubjects("/xml/create-app-expanse/searchSubjectsActiveResponse.xml");
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
        .response("/json/create-app-expanse/data-factory/last-laboratory-solution-add.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app-expanse/data-factory/solutionTypeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app-expanse/data-factory/applicationTypeResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "EXPANSE"))
        .response("/json/create-app-expanse/data-factory/applicationTypeExpanseResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app-expanse/data-factory/findLaboratoryResponse.json")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-app-expanse/dso/expanseIncludeSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("registration")
        .requestBody("/json/create-app-expanse/data-factory/createApplicationExpanseRequest.json")
        .response("{}")
        .build());

    var data = deserializeFormData("/json/create-app-expanse/form-data/start_event.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        testUserToken, data);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addApplicationActivityDefinitionKey)
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/addApplicationFormActivityPrePopulation.json"))
        .expectedVariables(
            Map.of("initiator", testUserName,
                "fullName", "testuser testuser testuser",
                StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addApplicationActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app-expanse/form-data/Activity_shared-add-application.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addFactorsActivityDefinitionKey)
        .formKey("shared-add-factors")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of(addApplicationActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addFactorsActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-add-factors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(checkComplianceActivityDefinitionKey)
        .formKey("shared-check-complience")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of(addFactorsActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(checkComplianceActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-check-compliance_noErrors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addDecisionIncludeActivityDefinitionKey)
        .formKey("shared-add-decision-include")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-solution-include-prepopulation.json"))
        .expectedVariables(Map.of(checkComplianceActivityDefinitionKey + "_completer", testUserName,
            "solutionTypeId", "ADD=SUCCESS"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addDecisionIncludeActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-add-decision-include.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addLetterDataActivityDefinitionKey)
        .formKey("shared-add-letter-data")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-solution-include-prepopulation.json"))
        .expectedVariables(
            Map.of(addDecisionIncludeActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addLetterDataActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-add-letter-data.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(signAppIncludeActivityDefinitionKey)
        .formKey("shared-sign-app-include")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/Activity_shared-sign-app-include_prepopulation.json"))
        .expectedVariables(Map.of(addLetterDataActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(signAppIncludeActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-sign-app-include.json")
        .build());

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);
    addExpectedVariable(signAppIncludeActivityDefinitionKey + "_completer", testUserName);
    addExpectedVariable("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Прийнято рішення про розширення факторів");

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/create-app-expanse/dso/expanseIncludeSystemSignatureCephContent.json");
    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/create-app-expanse.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testDenyingHappyPass_accreditationFlagIsTrue() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    var subjectStatusErrorActivityDefinitionKey = "Activity_shared-subject-status-error";
    var addApplicationActivityDefinitionKey = "Activity_shared-add-application";
    var addFactorsActivityDefinitionKey = "Activity_shared-add-factors";
    var checkComplianceActivityDefinitionKey = "Activity_shared-check-complience";
    var addDecisionDenyActivityDefinitionKey = "Activity_shared-add-decision-deny";
    var addLetterDataActivityDefinitionKey = "Activity_1eujure";
    var signAppDenyActivityDefinitionKey = "Activity_shared-sign-app-deny";

    stubSearchSubjects("/xml/create-app-expanse/searchSubjectsDisabledResponse.xml");
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
        .response("/json/create-app-expanse/data-factory/findLaboratoryResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "WO_CONSIDER"))
        .response("/json/create-app-expanse/data-factory/solutionTypeWoConsiderResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "EXPANSE"))
        .response("/json/create-app-expanse/data-factory/applicationTypeExpanseResponse.json")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-app-expanse/dso/expanseDenySystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("registration")
        .requestBody("/json/create-app-expanse/data-factory/createApplicationExpanseDenyRequest.json")
        .response("{}")
        .build());

    var data = deserializeFormData("/json/create-app-expanse/form-data/start_event.json");
    data.setAccessToken(testUserToken);
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        testUserToken, data);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(subjectStatusErrorActivityDefinitionKey)
        .formKey("shared-subject-status-error")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName,
            StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(subjectStatusErrorActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-subject-status-error.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addApplicationActivityDefinitionKey)
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/addApplicationFormActivityPrePopulation.json"))
        .expectedVariables(
            Map.of(subjectStatusErrorActivityDefinitionKey + "_completer", testUserName,
                "edrSuspendedOrCancelled", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addApplicationActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app-expanse/form-data/Activity_shared-add-application.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addFactorsActivityDefinitionKey)
        .formKey("shared-add-factors")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of(addApplicationActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addFactorsActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-add-factors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(checkComplianceActivityDefinitionKey)
        .formKey("shared-check-complience")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of(addFactorsActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(checkComplianceActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-check-compliance_errors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addDecisionDenyActivityDefinitionKey)
        .formKey("shared-add-decision-deny")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-solution-deny-prepopulation.json"))
        .expectedVariables(Map.of(checkComplianceActivityDefinitionKey + "_completer", testUserName,
            "solutionTypeId", "woconsider"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addDecisionDenyActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-add-decision-deny.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addLetterDataActivityDefinitionKey)
        .formKey("shared-add-letter-data")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-solution-deny-prepopulation.json"))
        .expectedVariables(
            Map.of(addDecisionDenyActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addLetterDataActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app-expanse/form-data/Activity_shared-add-letter-data.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(signAppDenyActivityDefinitionKey)
        .formKey("shared-sign-app-deny")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/Activity_shared-sign-app-deny_prepopulation.json"))
        .expectedVariables(Map.of(addLetterDataActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(signAppDenyActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app-expanse/form-data/Activity_shared-sign-app-deny.json")
        .build());

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";

    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);
    addExpectedVariable(signAppDenyActivityDefinitionKey + "_completer", testUserName);
    addExpectedVariable("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Прийнято рішення про залишення без розгляду");

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/create-app-expanse/dso/expanseDenySystemSignatureCephContent.json");
    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testValidationError() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    stubSearchSubjects("/xml/create-app-expanse/searchSubjectsActiveResponse.xml");
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
        .response("/json/create-app-expanse/data-factory/last-laboratory-solution-add.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app-expanse/data-factory/solutionTypeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app-expanse/data-factory/applicationTypeResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response(
            "/json/create-app-expanse/data-factory/findLaboratoryWithoutAccreditationResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-equal-laboratory-id-count")
        .queryParams(Map.of("laboratoryId", labId))
        .response("[]")
        .build());

    var data = deserializeFormData("/json/create-app-expanse/form-data/start_event.json");
    var resultMap = startProcessInstanceWithStartFormForError(PROCESS_DEFINITION_KEY, testUserToken,
        data);

    var errors = resultMap.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "laboratory"),
        Map.entry("message", "Додайте кадровий склад до лабораторії"),
        Map.entry("value", labId));
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testNoAccreditationFlag() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";
    var addApplicationActivityDefinitionKey = "Activity_shared-add-application";

    stubSearchSubjects("/xml/create-app-expanse/searchSubjectsActiveResponse.xml");
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
        .response("/json/create-app-expanse/data-factory/last-laboratory-solution-add.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app-expanse/data-factory/solutionTypeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app-expanse/data-factory/applicationTypeResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response(
            "/json/create-app-expanse/data-factory/findLaboratoryWithoutAccreditationResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-equal-laboratory-id-count")
        .queryParams(Map.of("laboratoryId", labId))
        .response("[{\"cnt\":1}]")
        .build());

    var data = deserializeFormData("/json/create-app-expanse/form-data/start_event.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        testUserToken, data);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addApplicationActivityDefinitionKey)
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-edrpou-prepopulation.json"))
        .expectedVariables(Map.of("initiator", testUserName,
            "fullName", "testuser testuser testuser",
            StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY))
        .build());
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testAppIsNotCreated() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    stubSearchSubjects("/xml/create-app-expanse/searchSubjectsActiveResponse.xml");
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
        .response("/json/create-app-expanse/data-factory/last-laboratory-solution-deny.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app-expanse/data-factory/solutionTypeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app-expanse/data-factory/applicationTypeResponse.json")
        .build());

    var data = deserializeFormData("/json/create-app-expanse/form-data/start_event.json");
    var errorMap = startProcessInstanceWithStartFormForError(PROCESS_DEFINITION_KEY, testUserToken,
        data);

    var errors = errorMap.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "laboratory"),
        Map.entry("message", "Заява на первинне внесення не створена"),
        Map.entry("value", labId));
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testSubjectIsDisabledButNoErrors() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    var subjectStatusErrorActivityDefinitionKey = "Activity_shared-subject-status-error";
    var addApplicationActivityDefinitionKey = "Activity_shared-add-application";
    var addFactorsActivityDefinitionKey = "Activity_shared-add-factors";
    var checkComplianceActivityDefinitionKey = "Activity_shared-check-complience";

    stubSearchSubjects("/xml/create-app-expanse/searchSubjectsDisabledResponse.xml");
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
        .response("/json/create-app-expanse/data-factory/findLaboratoryResponse.json")
        .build());

    var data = deserializeFormData("/json/create-app-expanse/form-data/start_event.json");
    data.setAccessToken(testUserToken);
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        testUserToken, data);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(subjectStatusErrorActivityDefinitionKey)
        .formKey("shared-subject-status-error")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName,
            StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(subjectStatusErrorActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-subject-status-error.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addApplicationActivityDefinitionKey)
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/addApplicationFormActivityPrePopulation.json"))
        .expectedVariables(
            Map.of(subjectStatusErrorActivityDefinitionKey + "_completer", testUserName,
                "edrSuspendedOrCancelled", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addApplicationActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app-expanse/form-data/Activity_shared-add-application.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addFactorsActivityDefinitionKey)
        .formKey("shared-add-factors")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of(addApplicationActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(addFactorsActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-add-factors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(checkComplianceActivityDefinitionKey)
        .formKey("shared-check-complience")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of(addFactorsActivityDefinitionKey + "_completer", testUserName))
        .build());
    var result = completeTaskWithError(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(checkComplianceActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-check-compliance_noErrors.json")
        .build());

    var errors = result.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "errorCheckFlag"),
        Map.entry("message",
            "Статус суб'єкта в ЄДР \"Скаcовано\" або \"Припинено\", оберіть відповідну причину відмови"),
        Map.entry("value", "false"));
  }
}
