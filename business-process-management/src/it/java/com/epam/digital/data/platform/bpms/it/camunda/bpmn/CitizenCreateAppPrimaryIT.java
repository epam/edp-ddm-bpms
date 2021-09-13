package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpms.camunda.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpms.camunda.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;

public class CitizenCreateAppPrimaryIT extends BaseBpmnIT {

  private final String PROCESS_DEFINITION_ID = "citizen-create-app-primary";

  @Value("${camunda.system-variables.const_dataFactoryBaseUrl}")
  private String dataFactoryBaseUrl;

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
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-create-app/data-factory/solutionTypeAddResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response(
            "/json/citizen-create-app/data-factory/applicationTypeResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
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
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-equal-laboratory-id-count")
        .queryParams(Map.of("laboratoryId", labId))
        .response("[{\"cnt\":1}]")
        .build());

    mockKeycloakGetUsersByRole("officer",
        "/json/citizen-create-app/keycloak/users-by-role-response.json");

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        //.headers(Map.of("X-Access-Token", headOfficerToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "WO_CONSIDER"))
        .response("/json/citizen-create-app/data-factory/solutionTypeWoConsiderResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", headOfficerToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
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

    var processInstanceId = startProcessInstanceAndGetId(labId);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    expectedVariablesMap.put("initiator", testUserName);
    expectedVariablesMap.put("fullName", "testuser testuser testuser");
    expectedVariablesMap.put("const_dataFactoryBaseUrl", dataFactoryBaseUrl);
    expectedVariablesMap.put("start_form_ceph_key", START_FORM_CEPH_KEY);

    assertWaitingActivity(processInstance, citizenSharedAddFactors, "citizen-shared-add-factors");
    completeTask(citizenSharedAddFactors, processInstanceId,
        "/json/citizen-create-app/form-data/Activity_citizen-shared-add-factors.json");
    addExpectedCephContent(processInstanceId, citizenSharedAddFactors,
        "/json/citizen-create-app/form-data/Activity_citizen-shared-add-factors.json");
    expectedVariablesMap.put(citizenSharedAddFactors + "_completer", testUserName);

    assertWaitingActivity(processInstance, citizenSharedSignFactors, "citizen-shared-sign-factors");
    completeTask(citizenSharedSignFactors, processInstanceId,
        "/json/citizen-create-app/form-data/Activity_citizen-shared-sign-factors.json");
    addExpectedCephContent(processInstanceId, citizenSharedSignFactors,
        "/json/citizen-create-app/form-data/Activity_citizen-shared-sign-factors.json");
    expectedVariablesMap.put(citizenSharedSignFactors + "_completer", testUserName);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-create-app/data-factory/solutionTypeAddResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response(
            "/json/citizen-create-app/data-factory/applicationTypeResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
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
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-equal-laboratory-id-count")
        .queryParams(Map.of("laboratoryId", labId))
        .response("[{\"cnt\":1}]")
        .build());

    mockKeycloakGetUsersByRole("officer",
        "/json/citizen-create-app/keycloak/users-by-role-response.json");

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", headOfficerToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-create-app/data-factory/solutionTypeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", headOfficerToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
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

    var processInstanceId = startProcessInstanceAndGetId(labId);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    expectedVariablesMap.put("initiator", testUserName);
    expectedVariablesMap.put("fullName", "testuser testuser testuser");
    expectedVariablesMap.put("const_dataFactoryBaseUrl", dataFactoryBaseUrl);
    expectedVariablesMap.put("start_form_ceph_key", START_FORM_CEPH_KEY);

    assertWaitingActivity(processInstance, citizenSharedAddFactors, "citizen-shared-add-factors");
    completeTask(citizenSharedAddFactors, processInstanceId,
        "/json/citizen-create-app/form-data/Activity_citizen-shared-add-factors.json");
    addExpectedCephContent(processInstanceId, citizenSharedAddFactors,
        "/json/citizen-create-app/form-data/Activity_citizen-shared-add-factors.json");
    expectedVariablesMap.put(citizenSharedAddFactors + "_completer", testUserName);

    assertWaitingActivity(processInstance, citizenSharedSignFactors, "citizen-shared-sign-factors");
    completeTask(citizenSharedSignFactors, processInstanceId,
        "/json/citizen-create-app/form-data/Activity_citizen-shared-sign-factors.json");
    addExpectedCephContent(processInstanceId, citizenSharedSignFactors,
        "/json/citizen-create-app/form-data/Activity_citizen-shared-sign-factors.json");
    expectedVariablesMap.put(citizenSharedSignFactors + "_completer", testUserName);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/citizen-create-app/data-factory/subjectResponse.json")
        .build());

    stubSearchSubjects("/xml/create-app/searchSubjectsActiveResponse.xml");

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/citizen-create-app/data-factory/last-laboratory-solution-add.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-create-app/data-factory/solutionTypeAddResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-create-app/data-factory/applicationTypeResponse.json")
        .build());

    var errorMap = startProcessInstanceForError(labId);

    var errors = errorMap.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "laboratory"),
        Map.entry("message", "Заява на первинне внесення вже створена"),
        Map.entry("value", labId));
  }

  @Test
  public void testValidationError() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/citizen-create-app/data-factory/subjectResponse.json")
        .build());

    stubSearchSubjects("/xml/create-app/searchSubjectsActiveResponse.xml");

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/citizen-create-app/data-factory/last-laboratory-solution-deny.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-create-app/data-factory/solutionTypeAddResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
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
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-equal-laboratory-id-count")
        .queryParams(Map.of("laboratoryId", labId))
        .response("[]")
        .build());

    var resultMap = startProcessInstanceForError(labId);

    var errors = resultMap.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "laboratory"),
        Map.entry("message", "Додайте кадровий склад до лабораторії"),
        Map.entry("value", labId));
  }

  @Test
  public void testWrongSubjectStatus() throws JsonProcessingException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/citizen-create-app/data-factory/subjectResponse.json")
        .build());

    stubSearchSubjects("/xml/citizen-add-lab/searchSubjectsCancelledResponse.xml");

    var result = startProcessInstanceForError(labId);

    var errors = result.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "laboratory"),
        Map.entry("message", "Статус в ЄДР 'Скаcовано' або 'Припинено'"),
        Map.entry("value", labId));
  }

  private String startProcessInstanceAndGetId(String labId)
      throws JsonProcessingException {
    createFormData(labId);
    return startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_ID, testUserToken,
        createFormData(labId));
  }

  private FormDataDto createFormData(String labId) {
    var data = new LinkedHashMap<String, Object>();
    data.put("laboratory", Map.of("laboratoryId", labId));
    data.put("edrpou", "77777777");
    data.put("subjectType", "LEGAL");
    data.put("subject", Map.of("subjectId", "activeSubject"));
    return FormDataDto.builder().data(data).build();
  }

  private Map<String, Map<String, List<Map<String, String>>>> startProcessInstanceForError(
      String labId) throws JsonProcessingException {
    var resultMap = startProcessInstanceWithStartForm(PROCESS_DEFINITION_ID,
        testUserToken, createFormData(labId));
    return (Map<String, Map<String, List<Map<String, String>>>>) resultMap;
  }

  @SneakyThrows
  private String addFieldToFormDataAndReturn(String form, String fieldName, Object filedValue) {
    var formData = TestUtils.getContent(form);
    var formDataMap = objectMapper.readValue(formData, Map.class);
    ((Map) formDataMap.get("data")).put(fieldName, filedValue);
    return objectMapper.writeValueAsString(formDataMap);
  }

  @SneakyThrows
  private String addFiledToJson(String json, String fieldName, Object filedValue) {
    var data = TestUtils.getContent(json);
    var formDataMap = objectMapper.readValue(data, Map.class);
    formDataMap.put(fieldName, filedValue);
    return objectMapper.writeValueAsString(formDataMap);
  }

  @SneakyThrows
  private String addFiledToSignatureFormData(String json, String fieldName, Object filedValue) {
    var data = TestUtils.getContent(json);
    var jsonDataMap = objectMapper.readValue(data, Map.class);
    var dataMap = objectMapper.readValue((String) jsonDataMap.get("data"), Map.class);
    dataMap.put(fieldName, filedValue);
    var dataMapStr = objectMapper.writeValueAsString(dataMap);
    jsonDataMap.put("data", dataMapStr);
    return objectMapper.writeValueAsString(jsonDataMap);
  }

}
