package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.historyService;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.bpms.camunda.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpms.camunda.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CitizenAttestationExcerptTest extends BaseBpmnTest {

  private static final String PROCESS_DEFINITION_KEY = "citizen-attestation-excerpt";

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptHappyPathTest() {
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .response("/json/citizen-attestation-excerpt/data-factory/searchSubjectsResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("subjectId1")
        .response("/json/citizen-attestation-excerpt/data-factory/subjectResponse.json")
        .build());
    mockEdrResponse("/json/citizen-attestation-excerpt/edr/searchSubjectsActiveResponse.json");
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratory1")
        .response("/json/citizen-attestation-excerpt/data-factory/laboratoryResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", "laboratory1"))
        .response(
            "/json/citizen-attestation-excerpt/data-factory/lastLaboratorySolutionAddResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addApplicationResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addSolutionTypeResponse.json")
        .build());
    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-attestation-excerpt/dso/systemSignatureRequest.json")
        .response("{\"signature\": \"signature\"}")
        .build());
    mockExcerptRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("excerpts")
        .requestBody("/json/citizen-attestation-excerpt/data-factory/generateExcerptRequest.json")
        .response("{\"excerptIdentifier\":\"d564f2ab-eec6-11eb-9efa-0a580a820439\"}")
        .build());
    mockExcerptStatusRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", TestUtils.getContent("/json/testuser2AccessToken.json")))
        .resource("excerpts")
        .resourceId("d564f2ab-eec6-11eb-9efa-0a580a820439")
        .response("{\"status\": \"COMPLETED\"}")
        .build());
    startProcessInstance("citizen-attestation-excerpt", Map.of("initiator", testUserName));

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .formKey("citizen-attestation-excerpt-bp-choose-lab")
        .assignee(testUserName)
        .expectedVariables(
            Map.of("initiator", testUserName, "subjectId", "subjectId1", "subjectName",
                "lab owner"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signAttestationExcerptActivity")
        .formKey("citizen-attestation-excerpt-bp-sign-excerpt")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivityPrePopulation.json"))
        .expectedVariables(
            Map.of("searchLabFormActivity_completer", testUserName, "solutionDate", "09-08-2021"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signAttestationExcerptActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json")
        .build());

    assertThat(currentProcessInstance).isWaitingAt("checkExcerptStatusActivity");

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(currentProcessInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);

    executeWaitingJob("checkExcerptStatusActivity");

    addExpectedVariable("signAttestationExcerptActivity_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_EXCERPT_ID,
        "d564f2ab-eec6-11eb-9efa-0a580a820439");
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Витяг сформовано");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptHappyPathNoEdrTest() {
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .response("/json/citizen-attestation-excerpt/data-factory/searchSubjectsResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("subjectId1")
        .response("/json/citizen-attestation-excerpt/data-factory/absentSubjectResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratory1")
        .response("/json/citizen-attestation-excerpt/data-factory/laboratoryResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", "laboratory1"))
        .response(
            "/json/citizen-attestation-excerpt/data-factory/lastLaboratorySolutionAddResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addApplicationResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addSolutionTypeResponse.json")
        .build());
    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody(
            "/json/citizen-attestation-excerpt/dso/absentSubjectSystemSignatureRequest.json")
        .response("{\"signature\": \"signature\"}")
        .build());
    mockExcerptRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("excerpts")
        .requestBody(
            "/json/citizen-attestation-excerpt/data-factory/absentSubjectGenerateExcerptRequest.json")
        .response("{\"excerptIdentifier\":\"d564f2ab-eec6-11eb-9efa-0a580a820439\"}")
        .build());
    mockExcerptStatusRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", TestUtils.getContent("/json/testuser2AccessToken.json")))
        .resource("excerpts")
        .resourceId("d564f2ab-eec6-11eb-9efa-0a580a820439")
        .response("{\"status\": \"COMPLETED\"}")
        .build());

    startProcessInstance("citizen-attestation-excerpt", Map.of("initiator", testUserName));

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .formKey("citizen-attestation-excerpt-bp-choose-lab")
        .assignee(testUserName)
        .expectedVariables(
            Map.of("initiator", testUserName, "subjectId", "subjectId1", "subjectName",
                "absent subject name"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signAttestationExcerptActivity")
        .formKey("citizen-attestation-excerpt-bp-sign-excerpt")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivityPrePopulation.json"))
        .expectedVariables(
            Map.of("searchLabFormActivity_completer", testUserName, "solutionDate", "09-08-2021"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signAttestationExcerptActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json")
        .build());

    assertThat(currentProcessInstance).isWaitingAt("checkExcerptStatusActivity");

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(currentProcessInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);

    executeWaitingJob("checkExcerptStatusActivity");

    addExpectedVariable("signAttestationExcerptActivity_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_EXCERPT_ID,
        "d564f2ab-eec6-11eb-9efa-0a580a820439");
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Витяг сформовано");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptGenerationFailedTest() {
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .response("/json/citizen-attestation-excerpt/data-factory/searchSubjectsResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("subjectId1")
        .response("/json/citizen-attestation-excerpt/data-factory/subjectResponse.json")
        .build());
    mockEdrResponse("/json/citizen-attestation-excerpt/edr/searchSubjectsActiveResponse.json");
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratory1")
        .response("/json/citizen-attestation-excerpt/data-factory/laboratoryResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", "laboratory1"))
        .response(
            "/json/citizen-attestation-excerpt/data-factory/lastLaboratorySolutionAddResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addApplicationResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addSolutionTypeResponse.json")
        .build());
    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-attestation-excerpt/dso/systemSignatureRequest.json")
        .response("{\"signature\": \"signature\"}")
        .build());
    mockExcerptRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("excerpts")
        .requestBody("/json/citizen-attestation-excerpt/data-factory/generateExcerptRequest.json")
        .response("{\"excerptIdentifier\":\"d564f2ab-eec6-11eb-9efa-0a580a820439\"}")
        .build());
    mockExcerptStatusRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", TestUtils.getContent("/json/testuser2AccessToken.json")))
        .resource("excerpts")
        .resourceId("d564f2ab-eec6-11eb-9efa-0a580a820439")
        .response("{\"status\": \"FAILED\"}")
        .build());

    startProcessInstance("citizen-attestation-excerpt", Map.of("initiator", testUserName));

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .formKey("citizen-attestation-excerpt-bp-choose-lab")
        .assignee(testUserName)
        .expectedVariables(
            Map.of("initiator", testUserName, "subjectId", "subjectId1", "subjectName",
                "lab owner"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signAttestationExcerptActivity")
        .formKey("citizen-attestation-excerpt-bp-sign-excerpt")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivityPrePopulation.json"))
        .expectedVariables(
            Map.of("searchLabFormActivity_completer", testUserName, "solutionDate", "09-08-2021"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signAttestationExcerptActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json")
        .build());

    assertThat(currentProcessInstance).isWaitingAt("checkExcerptStatusActivity");

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(currentProcessInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);

    executeWaitingJob("checkExcerptStatusActivity");

    addExpectedVariable("signAttestationExcerptActivity_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Витяг не сформовано");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptGenerationTimeoutTest() {
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .response("/json/citizen-attestation-excerpt/data-factory/searchSubjectsResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("subjectId1")
        .response("/json/citizen-attestation-excerpt/data-factory/subjectResponse.json")
        .build());
    mockEdrResponse("/json/citizen-attestation-excerpt/edr/searchSubjectsActiveResponse.json");
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratory1")
        .response("/json/citizen-attestation-excerpt/data-factory/laboratoryResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", "laboratory1"))
        .response(
            "/json/citizen-attestation-excerpt/data-factory/lastLaboratorySolutionAddResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addApplicationResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addSolutionTypeResponse.json")
        .build());
    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-attestation-excerpt/dso/systemSignatureRequest.json")
        .response("{\"signature\": \"signature\"}")
        .build());
    mockExcerptRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("excerpts")
        .requestBody("/json/citizen-attestation-excerpt/data-factory/generateExcerptRequest.json")
        .response("{\"excerptIdentifier\":\"d564f2ab-eec6-11eb-9efa-0a580a820439\"}")
        .build());
    mockExcerptStatusRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", TestUtils.getContent("/json/testuser2AccessToken.json")))
        .resource("excerpts")
        .resourceId("d564f2ab-eec6-11eb-9efa-0a580a820439")
        .response("{\"status\": \"IN_PROGRESS\"}")
        .build());
    mockExcerptStatusRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", TestUtils.getContent("/json/testuser2AccessToken.json")))
        .resource("excerpts")
        .resourceId("d564f2ab-eec6-11eb-9efa-0a580a820439")
        .response("{\"status\": \"IN_PROGRESS\"}")
        .build());

    startProcessInstance("citizen-attestation-excerpt", Map.of("initiator", testUserName));

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .formKey("citizen-attestation-excerpt-bp-choose-lab")
        .assignee(testUserName)
        .expectedVariables(
            Map.of("initiator", testUserName, "subjectId", "subjectId1", "subjectName",
                "lab owner"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signAttestationExcerptActivity")
        .formKey("citizen-attestation-excerpt-bp-sign-excerpt")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivityPrePopulation.json"))
        .expectedVariables(
            Map.of("searchLabFormActivity_completer", testUserName, "solutionDate", "09-08-2021"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signAttestationExcerptActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json")
        .build());

    assertThat(currentProcessInstance).isWaitingAt("checkExcerptStatusActivity");

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(currentProcessInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);

    executeWaitingJob("checkExcerptStatusActivity");
    assertThat(currentProcessInstance).isActive();

    executeWaitingJob("tryAgainEvent");
    assertThat(currentProcessInstance).isActive();

    executeWaitingJob("timeOutEvent");

    addExpectedVariable("signAttestationExcerptActivity_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Витяг не сформовано");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptApplicationNotAddedTest() {
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .response("/json/citizen-attestation-excerpt/data-factory/searchSubjectsResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("subjectId1")
        .response("/json/citizen-attestation-excerpt/data-factory/subjectResponse.json")
        .build());
    mockEdrResponse("/json/citizen-attestation-excerpt/edr/searchSubjectsActiveResponse.json");
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratory1")
        .response("/json/citizen-attestation-excerpt/data-factory/laboratoryResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", "laboratory1"))
        .response(
            "/json/citizen-attestation-excerpt/data-factory/lastLaboratorySolutionRefuseResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addApplicationResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addSolutionTypeResponse.json")
        .build());

    startProcessInstance("citizen-attestation-excerpt", Map.of("initiator", testUserName));

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .formKey("citizen-attestation-excerpt-bp-choose-lab")
        .assignee(testUserName)
        .expectedVariables(
            Map.of("initiator", testUserName, "subjectId", "subjectId1", "subjectName",
                "lab owner"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("noAttestationErrorActivity")
        .formKey("citizen-attestation-excerpt-bp-no-attestation-error")
        .assignee(testUserName)
        .expectedVariables(Map.of("searchLabFormActivity_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("noAttestationErrorActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/emptyFormData.json")
        .build());

    addExpectedVariable("noAttestationErrorActivity_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Витяг не сформовано");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptSubjectInEdrCancelledTest() {
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .response("/json/citizen-attestation-excerpt/data-factory/searchSubjectsResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("subjectId1")
        .response("/json/citizen-attestation-excerpt/data-factory/subjectResponse.json")
        .build());
    mockEdrResponse("/json/citizen-attestation-excerpt/edr/searchSubjectsCancelledResponse.json");

    startProcessInstance("citizen-attestation-excerpt", Map.of("initiator", testUserName));

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("subjectStatusErrorActivity")
        .formKey("citizen-attestation-excerpt-bp-status-error")
        .assignee(testUserName)
        .expectedVariables(
            Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("subjectStatusErrorActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-attestation-excerpt/form-data/emptyFormData.json")
        .build());

    addExpectedVariable("subjectStatusErrorActivity_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Витяг не сформовано");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptSubjectNotFoundInEdrCancelledTest() {
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .response("[]")
        .build());

    startProcessInstance("citizen-attestation-excerpt", Map.of("initiator", testUserName));

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("subjectNotFoundActivity")
        .formKey("citizen-attestation-excerpt-bp-no-subject-error")
        .assignee(testUserName)
        .expectedVariables(
            Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("subjectNotFoundActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-attestation-excerpt/form-data/emptyFormData.json")
        .build());

    addExpectedVariable("subjectNotFoundActivity_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Витяг не сформовано");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }
}
