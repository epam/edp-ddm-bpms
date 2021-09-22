package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.historyService;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CitizenAttestationExcerptTest extends BaseBpmnTest {

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

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("subjectId", "subjectId1");
    addExpectedVariable("subjectName", "lab owner");

    var searchLabFormDefinitionKey = "searchLabFormActivity";

    assertWaitingActivity(searchLabFormDefinitionKey, "citizen-attestation-excerpt-bp-choose-lab");

    completeTask(searchLabFormDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    addExpectedVariable("searchLabFormActivity_completer", testUserName);
    addExpectedVariable("solutionDate", "09-08-2021");
    addExpectedCephContent(searchLabFormDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    var signAttestationExcerptDefinitionKey = "signAttestationExcerptActivity";
    addExpectedCephContent(signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivityPrePopulation.json");
    assertWaitingActivity(signAttestationExcerptDefinitionKey,
        "citizen-attestation-excerpt-bp-sign-excerpt");

    completeTask(signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json");
    addExpectedCephContent(signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json");
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
    addExpectedVariable(Constants.SYS_VAR_PROCESS_EXCERPT_ID, "d564f2ab-eec6-11eb-9efa-0a580a820439");
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

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("subjectId", "subjectId1");
    addExpectedVariable("subjectName", "absent subject name");

    var searchLabFormDefinitionKey = "searchLabFormActivity";

    assertWaitingActivity(searchLabFormDefinitionKey, "citizen-attestation-excerpt-bp-choose-lab");

    completeTask(searchLabFormDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    addExpectedVariable("searchLabFormActivity_completer", testUserName);
    addExpectedVariable("solutionDate", "09-08-2021");
    addExpectedCephContent(searchLabFormDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    var signAttestationExcerptDefinitionKey = "signAttestationExcerptActivity";
    addExpectedCephContent(signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivityPrePopulation.json");
    assertWaitingActivity(signAttestationExcerptDefinitionKey,
        "citizen-attestation-excerpt-bp-sign-excerpt");

    completeTask(signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json");
    addExpectedCephContent(signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json");
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
    addExpectedVariable(Constants.SYS_VAR_PROCESS_EXCERPT_ID, "d564f2ab-eec6-11eb-9efa-0a580a820439");
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

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("subjectId", "subjectId1");
    addExpectedVariable("subjectName", "lab owner");

    var searchLabFormDefinitionKey = "searchLabFormActivity";

    assertWaitingActivity(searchLabFormDefinitionKey, "citizen-attestation-excerpt-bp-choose-lab");

    completeTask(searchLabFormDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    addExpectedVariable("searchLabFormActivity_completer", testUserName);
    addExpectedVariable("solutionDate", "09-08-2021");
    addExpectedCephContent(searchLabFormDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    var signAttestationExcerptDefinitionKey = "signAttestationExcerptActivity";
    addExpectedCephContent(signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivityPrePopulation.json");
    assertWaitingActivity(signAttestationExcerptDefinitionKey,
        "citizen-attestation-excerpt-bp-sign-excerpt");

    completeTask(signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json");
    addExpectedCephContent(signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json");
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

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("subjectId", "subjectId1");
    addExpectedVariable("subjectName", "lab owner");

    var searchLabFormDefinitionKey = "searchLabFormActivity";

    assertWaitingActivity(searchLabFormDefinitionKey, "citizen-attestation-excerpt-bp-choose-lab");

    completeTask(searchLabFormDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    addExpectedVariable("searchLabFormActivity_completer", testUserName);
    addExpectedVariable("solutionDate", "09-08-2021");
    addExpectedCephContent(searchLabFormDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    var signAttestationExcerptDefinitionKey = "signAttestationExcerptActivity";
    addExpectedCephContent(signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivityPrePopulation.json");
    assertWaitingActivity(signAttestationExcerptDefinitionKey,
        "citizen-attestation-excerpt-bp-sign-excerpt");

    completeTask(signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json");
    addExpectedCephContent(signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json");
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

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("subjectId", "subjectId1");
    addExpectedVariable("subjectName", "lab owner");

    var searchLabFormDefinitionKey = "searchLabFormActivity";

    assertWaitingActivity(searchLabFormDefinitionKey, "citizen-attestation-excerpt-bp-choose-lab");

    completeTask(searchLabFormDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    addExpectedVariable("searchLabFormActivity_completer", testUserName);
    addExpectedCephContent(searchLabFormDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    var noAttestationErrorDefinitionKey = "noAttestationErrorActivity";
    assertWaitingActivity(noAttestationErrorDefinitionKey,
        "citizen-attestation-excerpt-bp-no-attestation-error");

    completeTask(noAttestationErrorDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/emptyFormData.json");
    addExpectedCephContent(noAttestationErrorDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/emptyFormData.json");

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

    addExpectedVariable("initiator", testUserName);

    var noAttestationErrorDefinitionKey = "subjectStatusErrorActivity";
    assertWaitingActivity(noAttestationErrorDefinitionKey,
        "citizen-attestation-excerpt-bp-status-error");

    completeTask(noAttestationErrorDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/emptyFormData.json");
    addExpectedCephContent(noAttestationErrorDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/emptyFormData.json");

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

    addExpectedVariable("initiator", testUserName);

    var subjectNotFoundDefinitionKey = "subjectNotFoundActivity";
    assertWaitingActivity(subjectNotFoundDefinitionKey,
        "citizen-attestation-excerpt-bp-no-subject-error");

    completeTask(subjectNotFoundDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/emptyFormData.json");
    addExpectedCephContent(subjectNotFoundDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/emptyFormData.json");

    addExpectedVariable("subjectNotFoundActivity_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Витяг не сформовано");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);

    mockServer.verify();
  }
}
