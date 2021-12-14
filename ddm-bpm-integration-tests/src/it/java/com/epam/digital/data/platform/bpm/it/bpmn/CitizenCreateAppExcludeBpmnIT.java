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
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CitizenCreateAppExcludeBpmnIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_ID = "citizen-create-app-exclude";

  private final String headOfficerName = "headofficer";
  private final String officerUserName = "testuser2";
  private final String citizenUserName = "citizenuser";
  private String headOfficerToken;
  private String officerToken;
  private String citizenToken;

  @Before
  public void setUp() {
    headOfficerToken = TestUtils
        .getContent("/json/citizen-create-app-exclude/token/headOfficerAccessToken.json");
    officerToken = TestUtils
        .getContent("/json/citizen-create-app-exclude/token/officerAccessToken.json");
    citizenToken = TestUtils
        .getContent("/json/citizen-create-app-exclude/token/citizenAccessToken.json");
    mockConnectToKeycloak(officerRealm);
    mockKeycloakGetUsersByRole("officer", "[]");
    stubCitizenActivities();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-create-app-exclude.bpmn", "system-signature-bp"})
  public void testHappyPathWithHeadOfficerActivity() throws IOException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", headOfficerToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "EXCLUDE"))
        .response(
            "/json/citizen-create-app-exclude/data-factory/solutionTypeEqualConstantCodeExcludeResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", headOfficerToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "EXCLUDE"))
        .response(
            "/json/citizen-create-app-exclude/data-factory/applicationTypeEqualConstantCodeExcludeResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", headOfficerToken))
        .resource("registration")
        .requestBody("/json/citizen-create-app-exclude/data-factory/addRegistrationBody.json")
        .response("{}")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", headOfficerToken))
        .requestBody("/json/citizen-create-app-exclude/dso/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    var startFormData = deserializeFormData(
        "/json/citizen-create-app-exclude/form-data/startFormDataActivity.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_ID,
        citizenToken, startFormData);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("citizenSignActivity")
        .formKey("citizen-create-app-exclude-sign-app")
        .assignee(citizenUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app-exclude/form-data/startFormDataActivity.json"))
        .expectedVariables(Map.of("initiator", citizenUserName))
        .extensionElements(Map.of("eSign", "true", "ENTREPRENEUR", "true", "LEGAL", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("citizenSignActivity")
        .completerUserName(citizenUserName)
        .completerAccessToken(citizenToken)
        .expectedFormData(
            "/json/citizen-create-app-exclude/form-data/startFormDataActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("dispatchTaskActivity")
        .formKey("shared-dispatch-task")
        .candidateRoles(List.of("head-officer"))
        .extensionElements(Map.of("formVariables", "officerUsers"))
        .expectedVariables(Map.of("citizenSignActivity_completer", citizenUserName,
            "officerUsers", Collections.emptyList()))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("dispatchTaskActivity")
        .completerUserName(headOfficerName)
        .completerAccessToken(headOfficerToken)
        .expectedFormData("/json/citizen-create-app-exclude/form-data/dispatchTaskActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addApplicationFormActivity")
        .formKey("citizen-shared-add-application")
        .assignee(officerUserName)
        .expectedVariables(Map.of("dispatchTaskActivity_completer", headOfficerName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addApplicationFormActivity")
        .completerUserName(officerUserName)
        .completerAccessToken(officerToken)
        .expectedFormData(
            "/json/citizen-create-app-exclude/form-data/addApplicationFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("checkComplianceFormActivity")
        .formKey("citizen-create-app-exclude-bp-check-compliance")
        .assignee(officerUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app-exclude/form-data/addApplicationFormActivity.json"))
        .expectedVariables(Map.of("addApplicationFormActivity_completer", officerUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("checkComplianceFormActivity")
        .completerUserName(officerUserName)
        .completerAccessToken(officerToken)
        .expectedFormData(
            "/json/citizen-create-app-exclude/form-data/checkComplianceFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("officerSignFormActivity")
        .formKey("citizen-create-app-exclude-bp-officer-sign")
        .assignee(officerUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app-exclude/form-data/checkComplianceFormActivity.json"))
        .expectedVariables(Map.of("checkComplianceFormActivity_completer", officerUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("officerSignFormActivity")
        .completerUserName(officerUserName)
        .completerAccessToken(officerToken)
        .expectedFormData(
            "/json/citizen-create-app-exclude/form-data/checkComplianceFormActivity.json")
        .build());

    // headOfficerAgreeCheckFlag --> false
    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("headOfficerAgreeActivity")
        .formKey("citizen-create-app-exclude-bp-headofficer-compliance")
        .candidateRoles(List.of("head-officer"))
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app-exclude/form-data/checkComplianceFormActivity.json"))
        .expectedVariables(Map.of("officerSignFormActivity_completer", officerUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("headOfficerAgreeActivity")
        .completerUserName(headOfficerName)
        .completerAccessToken(headOfficerToken)
        .expectedFormData(
            "/json/citizen-create-app-exclude/form-data/headOfficerNotAgreeActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("checkComplianceFormActivity")
        .formKey("citizen-create-app-exclude-bp-check-compliance")
        .assignee(officerUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app-exclude/form-data/headOfficerNotAgreeActivity.json"))
        .expectedVariables(Map.of("addApplicationFormActivity_completer", officerUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("checkComplianceFormActivity")
        .completerUserName(officerUserName)
        .completerAccessToken(officerToken)
        .expectedFormData(
            "/json/citizen-create-app-exclude/form-data/checkComplianceFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("officerSignFormActivity")
        .formKey("citizen-create-app-exclude-bp-officer-sign")
        .assignee(officerUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app-exclude/form-data/checkComplianceFormActivity.json"))
        .expectedVariables(Map.of("checkComplianceFormActivity_completer", officerUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("officerSignFormActivity")
        .completerUserName(officerUserName)
        .completerAccessToken(officerToken)
        .expectedFormData(
            "/json/citizen-create-app-exclude/form-data/checkComplianceFormActivity.json")
        .build());

    // headOfficerAgreeCheckFlag --> true
    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("headOfficerAgreeActivity")
        .formKey("citizen-create-app-exclude-bp-headofficer-compliance")
        .candidateRoles(List.of("head-officer"))
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app-exclude/form-data/checkComplianceFormActivity.json"))
        .expectedVariables(Map.of("officerSignFormActivity_completer", officerUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("headOfficerAgreeActivity")
        .completerUserName(headOfficerName)
        .completerAccessToken(headOfficerToken)
        .expectedFormData(
            "/json/citizen-create-app-exclude/form-data/headOfficerAgreeActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addDecisionExcludeFormActivity")
        .formKey("create-app-exclude-bp-add-decision-exclude")
        .candidateRoles(List.of("head-officer"))
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app-exclude/form-data/addDecisionExcludeFormActivityPrePopulation.json"))
        .expectedVariables(Map.of("headOfficerAgreeActivity_completer", headOfficerName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addDecisionExcludeFormActivity")
        .completerUserName(headOfficerName)
        .completerAccessToken(headOfficerToken)
        .expectedFormData(
            "/json/citizen-create-app-exclude/form-data/addDecisionExcludeFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signAppExcludeFormActivity")
        .formKey("citizen-create-app-exclude-bp-sign-app-exclude")
        .candidateRoles(List.of("head-officer"))
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app-exclude/form-data/signAppExcludeFormActivity.json"))
        .expectedVariables(Map.of("addDecisionExcludeFormActivity_completer", headOfficerName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signAppExcludeFormActivity")
        .completerUserName(headOfficerName)
        .completerAccessToken(headOfficerToken)
        .expectedFormData(
            "/json/citizen-create-app-exclude/form-data/signAppExcludeFormActivity.json")
        .build());

    addExpectedVariable("signAppExcludeFormActivity_completer", headOfficerName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Лабораторію видалено з реєстру");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-create-app-exclude.bpmn", "system-signature-bp"})
  public void testDenyPathWithoutHeadOfficerActivity() throws IOException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", officerToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "WO_CONSIDER"))
        .response(
            "/json/citizen-create-app-exclude/data-factory/solutionTypeEqualConstantCodeWoConsiderResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", headOfficerToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "EXCLUDE"))
        .response(
            "/json/citizen-create-app-exclude/data-factory/applicationTypeEqualConstantCodeExcludeResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", headOfficerToken))
        .resource("registration")
        .requestBody(
            "/json/citizen-create-app-exclude/data-factory/addRegistrationNoConsiderBody.json")
        .response("{}")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", headOfficerToken))
        .requestBody(
            "/json/citizen-create-app-exclude/dso/digitalSignatureNoConsiderRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    var startFormData = deserializeFormData(
        "/json/citizen-create-app-exclude/form-data/startFormDataActivity.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_ID,
        citizenToken, startFormData);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("citizenSignActivity")
        .formKey("citizen-create-app-exclude-sign-app")
        .assignee(citizenUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app-exclude/form-data/startFormDataActivity.json"))
        .expectedVariables(Map.of("initiator", citizenUserName))
        .extensionElements(Map.of("eSign", "true", "ENTREPRENEUR", "true", "LEGAL", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("citizenSignActivity")
        .completerUserName(citizenUserName)
        .completerAccessToken(citizenToken)
        .expectedFormData(
            "/json/citizen-create-app-exclude/form-data/startFormDataActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("dispatchTaskActivity")
        .formKey("shared-dispatch-task")
        .candidateRoles(List.of("head-officer"))
        .extensionElements(Map.of("formVariables", "officerUsers"))
        .expectedVariables(Map.of("citizenSignActivity_completer", citizenUserName,
            "officerUsers", Collections.emptyList()))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("dispatchTaskActivity")
        .completerUserName(headOfficerName)
        .completerAccessToken(headOfficerToken)
        .expectedFormData("/json/citizen-create-app-exclude/form-data/dispatchTaskActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addApplicationFormActivity")
        .formKey("citizen-shared-add-application")
        .assignee(officerUserName)
        .expectedVariables(Map.of("dispatchTaskActivity_completer", headOfficerName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addApplicationFormActivity")
        .completerUserName(officerUserName)
        .completerAccessToken(officerToken)
        .expectedFormData(
            "/json/citizen-create-app-exclude/form-data/addApplicationFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("checkComplianceFormActivity")
        .formKey("citizen-create-app-exclude-bp-check-compliance")
        .assignee(officerUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app-exclude/form-data/addApplicationFormActivity.json"))
        .expectedVariables(Map.of("addApplicationFormActivity_completer", officerUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("checkComplianceFormActivity")
        .completerUserName(officerUserName)
        .completerAccessToken(officerToken)
        .expectedFormData(
            "/json/citizen-create-app-exclude/form-data/checkComplianceWithErrorFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("officerSignFormActivity")
        .formKey("citizen-create-app-exclude-bp-officer-sign")
        .assignee(officerUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app-exclude/form-data/checkComplianceWithErrorFormActivity.json"))
        .expectedVariables(Map.of("checkComplianceFormActivity_completer", officerUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("officerSignFormActivity")
        .completerUserName(officerUserName)
        .completerAccessToken(officerToken)
        .expectedFormData(
            "/json/citizen-create-app-exclude/form-data/checkComplianceWithErrorFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addDecisionDenyFormActivity")
        .formKey("shared-add-decision-deny")
        .candidateRoles(List.of("head-officer"))
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app-exclude/form-data/addDecisionDenyFormActivityPrePopulation.json"))
        .expectedVariables(Map.of("officerSignFormActivity_completer", officerUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addDecisionDenyFormActivity")
        .completerUserName(headOfficerName)
        .completerAccessToken(headOfficerToken)
        .expectedFormData(
            "/json/citizen-create-app-exclude/form-data/addDecisionDenyFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signAppDenyFormActivity")
        .formKey("citizen-create-app-exclude-bp-sign-app-deny")
        .candidateRoles(List.of("head-officer"))
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app-exclude/form-data/signAppDenyFormActivity.json"))
        .expectedVariables(Map.of("addDecisionDenyFormActivity_completer", headOfficerName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signAppDenyFormActivity")
        .completerUserName(headOfficerName)
        .completerAccessToken(headOfficerToken)
        .expectedFormData(
            "/json/citizen-create-app-exclude/form-data/signAppDenyFormActivity.json")
        .build());

    addExpectedVariable("signAppDenyFormActivity_completer", headOfficerName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Залишено без розгляду");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-create-app-exclude.bpmn", "system-signature-bp"})
  public void testValidationError() throws IOException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", citizenToken))
        .resource("last-laboratory-solution")
        .queryParams(Maps.of("laboratoryId", "3fa85f64-5717-4562-b3fc-2c963f66afa6"))
        .response(
            "/json/citizen-create-app-exclude/data-factory/lastLaboratorySolutionDenyResponse.json")
        .build());

    var startFormData = deserializeFormData(
        "/json/citizen-create-app-exclude/form-data/startFormDataActivity.json");
    var resultMap = startProcessInstanceWithStartFormForError(PROCESS_DEFINITION_ID,
        citizenToken, startFormData);

    var errors = resultMap.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "laboratory"),
        Map.entry("message",
            "Немає заяви на первинне внесення або заява на видалення вже створена"),
        Map.entry("value", "3fa85f64-5717-4562-b3fc-2c963f66afa6"));
  }

  private void stubCitizenActivities() {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", citizenToken))
        .resource("last-laboratory-solution")
        .queryParams(Maps.of("laboratoryId", "3fa85f64-5717-4562-b3fc-2c963f66afa6"))
        .response(
            "/json/citizen-create-app-exclude/data-factory/lastLaboratorySolutionResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", citizenToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "ADD"))
        .response(
            "/json/citizen-create-app-exclude/data-factory/applicationTypeEqualConstantCodeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", citizenToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "ADD"))
        .response(
            "/json/citizen-create-app-exclude/data-factory/solutionTypeEqualConstantCodeAddResponse.json")
        .build());
  }
}
