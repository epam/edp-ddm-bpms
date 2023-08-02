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
import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CitizenCreateAppPrimaryIT extends BaseBpmnIT {

  private final String PROCESS_DEFINITION_KEY = "citizen-create-app-primary";

  private final String officerUserName = "officerusername";
  private final String headOfficerUserName = "headofficer";
  private String headOfficerToken;
  private String officerToken;

  @Before
  public void init() {
    super.init();
    headOfficerToken = TestUtils.getContent("/json/citizen-create-app/headOfficerAccessToken.json");
    officerToken = TestUtils.getContent("/json/citizen-create-app/officerAccessToken.json");
    mockConnectToKeycloak(officerRealm);
  }

  @Test
  public void woConsiderFlowWithHeadOfficerAgreeFlagTrue() throws JsonProcessingException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";
    var createdDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    var citizenSharedAddFactors = "Activity_citizen-shared-add-factors";
    var citizenSharedSignFactors = "Activity_citizen-shared-sign-factors";
    var sharedDispatchTask = "Activity_shared-dispatch-task";
    var citizenSharedAddApplication = "Activity_citizen-shared-add-application";
    var citizenSharedCheckComplience = "Activity_citizen-shared-check-complience";
    var citizenSharedOfficerSignApp = "Activity_citizen-shared-officer-sign-app";
    var citizenSharedHeadofficerCheckComplience = "Activity_citizen-shared-headofficer-check-complience";
    var sharedAddDecisionDeny = "Activity_shared-add-decision-deny";
    var citizenSharedSignAppDeny = "Activity_citizen-shared-sign-app-deny";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/citizen-create-app/data-factory/subjectResponse.json")
        .build());
    stubSearchSubjects("/xml/citizen-create-app/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
        .response("/json/citizen-create-app/data-factory/solutionTypeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
        .response(
            "/json/citizen-create-app/data-factory/applicationTypeResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .requestBody("{\"laboratoryId\":\"" + labId + "\"}")
        .response("/json/citizen-create-app/data-factory/lastLaboratorySolutionDeny.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response(
            "/json/citizen-create-app/data-factory/labWithoutAccreditation.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-equal-laboratory-id-count")
        .requestBody("{\"laboratoryId\":\"" + labId + "\"}")
        .response("[{\"cnt\":1}]")
        .build());
    mockKeycloakGetUsersByRole("officer",
        "/json/citizen-create-app/keycloak/users-by-role-response.json");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        //.headers(Map.of("X-Access-Token", headOfficerToken))
        .resource("solution-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"WO_CONSIDER\"}")
        .response("/json/citizen-create-app/data-factory/solutionTypeWoConsiderResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", headOfficerToken))
        .resource("application-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
        .response(
            "/json/citizen-create-app/data-factory/applicationTypeResponse.json")
        .build());
    var sinResponseBody = addFiledToSignatureFormData(
        "/json/citizen-create-app/dso/primaryDenySystemSignatureRequest.json", "createdDate",
        createdDate);
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", headOfficerToken))
        .requestBody(sinResponseBody)
        .response("{\"signature\": \"test\"}")
        .build());
    var responseBody = addFiledToJson(
        "/json/citizen-create-app/data-factory/createApplicationDenyRequest.json",
        "createdDate", createdDate);
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", headOfficerToken))
        .resource("registration")
        .requestBody(responseBody)
        .response("{}")
        .build());

    var startFormData = deserializeFormData(
        "/json/citizen-create-app/form-data/startFormDataActivity.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        testUserToken, startFormData);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedAddFactors)
        .formKey("citizen-shared-add-factors")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-add-factors-pre-population.json"))
        .expectedVariables(Map.of("initiator", testUserName,
            StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY,
            "fullName", "testuser testuser testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedAddFactors)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-add-factors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedSignFactors)
        .formKey("citizen-shared-sign-factors")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-add-factors.json"))
        .expectedVariables(Map.of(citizenSharedAddFactors + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedSignFactors)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-sign-factors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(sharedDispatchTask)
        .formKey("shared-dispatch-task")
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(sharedDispatchTask)
        .completerUserName(headOfficerUserName)
        .completerAccessToken(headOfficerToken)
        .expectedFormData("/json/citizen-create-app/form-data/Activity_shared-dispatch-task.json")
        .build());

    var citizenSharedAddApplicationPrePopulation = deserializeFormData(
        "/json/citizen-create-app/form-data/Activity_citizen-shared-add-application-pre-population.json");
    citizenSharedAddApplicationPrePopulation.getData().put("createdDate", createdDate);
    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedAddApplication)
        .formKey("citizen-shared-add-application")
        .assignee(officerUserName)
        .expectedVariables(Map.of(sharedDispatchTask + "_completer", headOfficerUserName))
        .expectedFormDataPrePopulation(citizenSharedAddApplicationPrePopulation)
        .build());
    var formDataAsString = addFieldToFormDataAndReturn(
        "/json/citizen-create-app/form-data/Activity_citizen-shared-add-application.json",
        "createdDate", createdDate);
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedAddApplication)
        .completerUserName(officerUserName)
        .completerAccessToken(officerToken)
        .expectedFormData(formDataAsString)
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedCheckComplience)
        .formKey("citizen-shared-check-complience")
        .assignee(officerUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-check-complience-pre-population.json"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedCheckComplience)
        .completerUserName(officerUserName)
        .completerAccessToken(officerToken)
        .expectedFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-check-complience-with-errors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedOfficerSignApp)
        .formKey("citizen-shared-officer-sign-app")
        .assignee(officerUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-check-complience-with-errors.json"))
        .expectedVariables(Map.of(citizenSharedCheckComplience + "_completer", officerUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedOfficerSignApp)
        .completerUserName(officerUserName)
        .completerAccessToken(officerToken)
        .expectedFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-officer-sign-app-with-errors.json")
        .build());

    /*approve flow starts*/
    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedHeadofficerCheckComplience)
        .formKey("citizen-shared-headofficer-check-complience")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-headofficer-check-complience-pre-population-with-errors.json"))
        .expectedVariables(Map.of(citizenSharedOfficerSignApp + "_completer", officerUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedHeadofficerCheckComplience)
        .completerUserName(headOfficerUserName)
        .completerAccessToken(headOfficerToken)
        .expectedFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-headofficer-check-complience-with-errors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedCheckComplience)
        .formKey("citizen-shared-check-complience")
        .assignee(officerUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-check-complience-pre-population-comment.json"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedCheckComplience)
        .completerUserName(officerUserName)
        .completerAccessToken(officerToken)
        .expectedFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-check-complience-with-errors-without-approve.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedOfficerSignApp)
        .formKey("citizen-shared-officer-sign-app")
        .assignee(officerUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-check-complience-with-errors-without-approve.json"))
        .expectedVariables(Map.of(citizenSharedCheckComplience + "_completer", officerUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedOfficerSignApp)
        .completerUserName(officerUserName)
        .completerAccessToken(officerToken)
        .expectedFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-officer-sign-app-with-errors-without-approve.json")
        .build());
    /*approve flow ends*/

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(sharedAddDecisionDeny)
        .formKey("shared-add-decision-deny")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app/form-data/Activity_shared-add-decision-deny-pre-population.json"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(sharedAddDecisionDeny)
        .completerUserName(headOfficerUserName)
        .completerAccessToken(headOfficerToken)
        .expectedFormData(
            "/json/citizen-create-app/form-data/Activity_shared-add-decision-deny.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedSignAppDeny)
        .formKey("citizen-shared-sign-app-deny")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-sign-app-deny-pre-population.json"))
        .expectedVariables(Map.of(sharedAddDecisionDeny + "_completer", headOfficerUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedSignAppDeny)
        .completerUserName(headOfficerUserName)
        .completerAccessToken(headOfficerToken)
        .expectedFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-sign-app-deny.json")
        .build());

    assertThat(processInstance).isEnded();
  }

  @Test
  public void happyPath() throws JsonProcessingException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";
    var createdDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    var citizenSharedAddFactors = "Activity_citizen-shared-add-factors";
    var citizenSharedSignFactors = "Activity_citizen-shared-sign-factors";
    var sharedDispatchTask = "Activity_shared-dispatch-task";
    var citizenSharedAddApplication = "Activity_citizen-shared-add-application";
    var citizenSharedCheckComplience = "Activity_citizen-shared-check-complience";
    var citizenSharedOfficerSignApp = "Activity_citizen-shared-officer-sign-app";
    var citizenSharedHeadofficerCheckComplience = "Activity_citizen-shared-headofficer-check-complience";
    var sharedAddDecisionInclude = "Activity_shared-add-decision-include";
    var citizenSharedSignAppInclude = "Activity_citizen-shared-sign-app-include";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/citizen-create-app/data-factory/subjectResponse.json")
        .build());
    stubSearchSubjects("/xml/citizen-create-app/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
        .response("/json/citizen-create-app/data-factory/solutionTypeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
        .response(
            "/json/citizen-create-app/data-factory/applicationTypeResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .requestBody("{\"laboratoryId\":\"" + labId + "\"}")
        .response("/json/citizen-create-app/data-factory/lastLaboratorySolutionDeny.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response(
            "/json/citizen-create-app/data-factory/labWithoutAccreditation.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-equal-laboratory-id-count")
        .requestBody("{\"laboratoryId\":\"" + labId + "\"}")
        .response("[{\"cnt\":1}]")
        .build());
    mockKeycloakGetUsersByRole("officer",
        "/json/citizen-create-app/keycloak/users-by-role-response.json");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", headOfficerToken))
        .resource("solution-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
        .response("/json/citizen-create-app/data-factory/solutionTypeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", headOfficerToken))
        .resource("application-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
        .response(
            "/json/citizen-create-app/data-factory/applicationTypeResponse.json")
        .build());
    var sinResponseBody = addFiledToSignatureFormData(
        "/json/citizen-create-app/dso/primaryIncludeSystemSignatureRequest.json", "createdDate",
        createdDate);
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", headOfficerToken))
        .requestBody(sinResponseBody)
        .response("{\"signature\": \"test\"}")
        .build());
    var responseBody = addFiledToJson(
        "/json/citizen-create-app/data-factory/createApplicationIncludeRequest.json",
        "createdDate", createdDate);
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", headOfficerToken))
        .resource("registration")
        .requestBody(responseBody)
        .response("{}")
        .build());

    var startFormData = deserializeFormData(
        "/json/citizen-create-app/form-data/startFormDataActivity.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        testUserToken, startFormData);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedAddFactors)
        .formKey("citizen-shared-add-factors")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-add-factors-pre-population.json"))
        .expectedVariables(Map.of("initiator", testUserName,
            StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY,
            "fullName", "testuser testuser testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedAddFactors)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-add-factors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedSignFactors)
        .formKey("citizen-shared-sign-factors")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-add-factors.json"))
        .expectedVariables(Map.of(citizenSharedAddFactors + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedSignFactors)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-sign-factors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(sharedDispatchTask)
        .formKey("shared-dispatch-task")
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(sharedDispatchTask)
        .completerUserName(headOfficerUserName)
        .completerAccessToken(headOfficerToken)
        .expectedFormData("/json/citizen-create-app/form-data/Activity_shared-dispatch-task.json")
        .build());

    var citizenSharedAddApplicationPrePopulation = deserializeFormData(
        "/json/citizen-create-app/form-data/Activity_citizen-shared-add-application-pre-population.json");
    citizenSharedAddApplicationPrePopulation.getData().put("createdDate", createdDate);
    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedAddApplication)
        .formKey("citizen-shared-add-application")
        .assignee(officerUserName)
        .expectedVariables(Map.of(sharedDispatchTask + "_completer", headOfficerUserName))
        .expectedFormDataPrePopulation(citizenSharedAddApplicationPrePopulation)
        .build());
    var formDataAsString = addFieldToFormDataAndReturn(
        "/json/citizen-create-app/form-data/Activity_citizen-shared-add-application.json",
        "createdDate", createdDate);
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedAddApplication)
        .completerUserName(officerUserName)
        .completerAccessToken(officerToken)
        .expectedFormData(formDataAsString)
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedCheckComplience)
        .formKey("citizen-shared-check-complience")
        .assignee(officerUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-check-complience-pre-population.json"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedCheckComplience)
        .completerUserName(officerUserName)
        .completerAccessToken(officerToken)
        .expectedFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-check-complience.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedOfficerSignApp)
        .formKey("citizen-shared-officer-sign-app")
        .assignee(officerUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-check-complience.json"))
        .expectedVariables(Map.of(citizenSharedCheckComplience + "_completer", officerUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedOfficerSignApp)
        .completerUserName(officerUserName)
        .completerAccessToken(officerToken)
        .expectedFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-officer-sign-app.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedHeadofficerCheckComplience)
        .formKey("citizen-shared-headofficer-check-complience")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-headofficer-check-complience-pre-population.json"))
        .expectedVariables(Map.of(citizenSharedOfficerSignApp + "_completer", officerUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedHeadofficerCheckComplience)
        .completerUserName(headOfficerUserName)
        .completerAccessToken(headOfficerToken)
        .expectedFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-headofficer-check-complience.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(sharedAddDecisionInclude)
        .formKey("shared-add-decision-include")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app/form-data/Activity_shared-add-decision-include-pre-population.json"))
        .expectedVariables(
            Map.of(citizenSharedHeadofficerCheckComplience + "_completer", headOfficerUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(sharedAddDecisionInclude)
        .completerUserName(headOfficerUserName)
        .completerAccessToken(headOfficerToken)
        .expectedFormData(
            "/json/citizen-create-app/form-data/Activity_shared-add-decision-include.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedSignAppInclude)
        .formKey("citizen-shared-sign-app-include")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-sign-app-include-pre-population.json"))
        .expectedVariables(Map.of(sharedAddDecisionInclude + "_completer", headOfficerUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId(citizenSharedSignAppInclude)
        .completerUserName(headOfficerUserName)
        .completerAccessToken(headOfficerToken)
        .expectedFormData(
            "/json/citizen-create-app/form-data/Activity_citizen-shared-sign-app-include.json")
        .build());

    assertThat(processInstance).isEnded();
  }

  @Test
  public void testAppAlreadyCreated() throws JsonProcessingException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/citizen-create-app/data-factory/subjectResponse.json")
        .build());
    stubSearchSubjects("/xml/create-app/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .requestBody("{\"laboratoryId\":\"" + labId + "\"}")
        .response("/json/citizen-create-app/data-factory/last-laboratory-solution-add.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
        .response("/json/citizen-create-app/data-factory/solutionTypeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
        .response("/json/citizen-create-app/data-factory/applicationTypeResponse.json")
        .build());

    var startFormData = deserializeFormData(
        "/json/citizen-create-app/form-data/startFormDataActivity.json");
    var errorMap = startProcessInstanceWithStartFormForError(PROCESS_DEFINITION_KEY, testUserToken,
        startFormData);

    var errors = errorMap.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "laboratory"),
        Map.entry("message", "Заява на первинне внесення вже створена"),
        Map.entry("value", labId));
  }

  @Test
  public void testValidationError() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/citizen-create-app/data-factory/subjectResponse.json")
        .build());
    stubSearchSubjects("/xml/create-app/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .requestBody("{\"laboratoryId\":\"" + labId + "\"}")
        .response("/json/citizen-create-app/data-factory/last-laboratory-solution-deny.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
        .response("/json/citizen-create-app/data-factory/solutionTypeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
        .response("/json/citizen-create-app/data-factory/applicationTypeResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response(
            "/json/citizen-create-app/data-factory/labWithoutAccreditation.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-equal-laboratory-id-count")
        .requestBody("{\"laboratoryId\":\"" + labId + "\"}")
        .response("[]")
        .build());

    var startFormData = deserializeFormData(
        "/json/citizen-create-app/form-data/startFormDataActivity.json");
    var resultMap = startProcessInstanceWithStartFormForError(PROCESS_DEFINITION_KEY, testUserToken,
        startFormData);

    var errors = resultMap.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "laboratory"),
        Map.entry("message", "Додайте кадровий склад до лабораторії"),
        Map.entry("value", labId));
  }

  @Test
  public void testWrongSubjectStatus() throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/citizen-create-app/data-factory/subjectResponse.json")
        .build());
    stubSearchSubjects("/xml/citizen-add-lab/searchSubjectsCancelledResponse.xml");

    var startFormData = deserializeFormData(
        "/json/citizen-create-app/form-data/startFormDataActivity.json");
    var result = startProcessInstanceWithStartFormForError(PROCESS_DEFINITION_KEY, testUserToken,
        startFormData);

    var errors = result.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "laboratory"),
        Map.entry("message", "Статус в ЄДР 'Скаcовано' або 'Припинено'"),
        Map.entry("value", "bb652d3f-a36f-465a-b7ba-232a5a1680c5"));
  }
}
