package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.historyService;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

public class CitizenAttestationExcerptIT extends BaseBpmnIT {

  @Before
  public void setUp() {
    keycloakMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/auth/realms/system-user-realm/protocol/openid-connect/token"))
            .withRequestBody(equalTo("grant_type=client_credentials"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(convertJsonToString(
                    "/json/keycloak/keycloakSystemUserConnectResponse.json")))));
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptHappyPathTest() throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .response("/json/citizen-attestation-excerpt/data-factory/searchSubjectsResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("subjectId1")
        .response("/json/citizen-attestation-excerpt/data-factory/subjectResponse.json")
        .build());

    stubSearchSubjects("/xml/citizen-attestation-excerpt/searchSubjectsActiveResponse.xml");

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratory1")
        .response("/json/citizen-attestation-excerpt/data-factory/laboratoryResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", "laboratory1"))
        .response(
            "/json/citizen-attestation-excerpt/data-factory/lastLaboratorySolutionAddResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addApplicationResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addSolutionTypeResponse.json")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-attestation-excerpt/dso/systemSignatureRequest.json")
        .response("{\"signature\": \"signature\"}")
        .build());

    stubExcerptServiceRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("excerpts")
        .requestBody("/json/citizen-attestation-excerpt/data-factory/generateExcerptRequest.json")
        .response("{\"excerptIdentifier\":\"d564f2ab-eec6-11eb-9efa-0a580a820439\"}")
        .build());

    stubExcerptServiceRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", TestUtils.getContent("/json/testuser2AccessToken.json")))
        .uri(UriComponentsBuilder.fromPath(EXCERPT_SERVICE_MOCK_SERVER)
            .pathSegment("excerpts", "d564f2ab-eec6-11eb-9efa-0a580a820439", "status"))
        .response("{\"status\": \"COMPLETED\"}")
        .build());

    var processInstanceId = startProcessInstance("citizen-attestation-excerpt", testUserToken);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("subjectId", "subjectId1");
    addExpectedVariable("subjectName", "lab owner");
    addExpectedVariable("const_dataFactoryBaseUrl", "http://localhost:8877/mock-server");

    var searchLabFormDefinitionKey = "searchLabFormActivity";

    assertWaitingActivity(processInstance, searchLabFormDefinitionKey,
        "citizen-attestation-excerpt-bp-choose-lab");

    completeTask(searchLabFormDefinitionKey, processInstanceId,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    addExpectedVariable("searchLabFormActivity_completer", testUserName);
    addExpectedVariable("solutionDate", "09-08-2021");
    addExpectedCephContent(processInstanceId, searchLabFormDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    var signAttestationExcerptDefinitionKey = "signAttestationExcerptActivity";
    addExpectedCephContent(processInstanceId, signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivityPrePopulation.json");
    assertWaitingActivity(processInstance, signAttestationExcerptDefinitionKey,
        "citizen-attestation-excerpt-bp-sign-excerpt");

    completeTask(signAttestationExcerptDefinitionKey, processInstanceId,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json");
    addExpectedCephContent(processInstanceId, signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json");
    assertThat(processInstance).isWaitingAt("checkExcerptStatusActivity");

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);

    executeWaitingJob("checkExcerptStatusActivity");

    addExpectedVariable("signAttestationExcerptActivity_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_EXCERPT_ID,
        "d564f2ab-eec6-11eb-9efa-0a580a820439");
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Витяг сформовано");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptHappyPathNoEdrTest() throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .response("/json/citizen-attestation-excerpt/data-factory/searchSubjectsResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("subjectId1")
        .response("/json/citizen-attestation-excerpt/data-factory/absentSubjectResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratory1")
        .response("/json/citizen-attestation-excerpt/data-factory/laboratoryResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", "laboratory1"))
        .response(
            "/json/citizen-attestation-excerpt/data-factory/lastLaboratorySolutionAddResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addApplicationResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addSolutionTypeResponse.json")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody(
            "/json/citizen-attestation-excerpt/dso/absentSubjectSystemSignatureRequest.json")
        .response("{\"signature\": \"signature\"}")
        .build());

    stubExcerptServiceRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("excerpts")
        .requestBody(
            "/json/citizen-attestation-excerpt/data-factory/absentSubjectGenerateExcerptRequest.json")
        .response("{\"excerptIdentifier\":\"d564f2ab-eec6-11eb-9efa-0a580a820439\"}")
        .build());

    stubExcerptServiceRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", TestUtils.getContent("/json/testuser2AccessToken.json")))
        .uri(UriComponentsBuilder.fromPath(EXCERPT_SERVICE_MOCK_SERVER)
            .pathSegment("excerpts", "d564f2ab-eec6-11eb-9efa-0a580a820439", "status"))
        .response("{\"status\": \"COMPLETED\"}")
        .build());

    var processInstanceId = startProcessInstance("citizen-attestation-excerpt", testUserToken);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("subjectId", "subjectId1");
    addExpectedVariable("subjectName", "absent subject name");
    addExpectedVariable("const_dataFactoryBaseUrl", "http://localhost:8877/mock-server");

    var searchLabFormDefinitionKey = "searchLabFormActivity";

    assertWaitingActivity(processInstance, searchLabFormDefinitionKey,
        "citizen-attestation-excerpt-bp-choose-lab");

    completeTask(searchLabFormDefinitionKey, processInstanceId,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    addExpectedVariable("searchLabFormActivity_completer", testUserName);
    addExpectedVariable("solutionDate", "09-08-2021");
    addExpectedCephContent(processInstanceId, searchLabFormDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    var signAttestationExcerptDefinitionKey = "signAttestationExcerptActivity";
    addExpectedCephContent(processInstanceId, signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivityPrePopulation.json");
    assertWaitingActivity(processInstance, signAttestationExcerptDefinitionKey,
        "citizen-attestation-excerpt-bp-sign-excerpt");

    completeTask(signAttestationExcerptDefinitionKey, processInstanceId,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json");
    addExpectedCephContent(processInstanceId, signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json");
    assertThat(processInstance).isWaitingAt("checkExcerptStatusActivity");

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);

    executeWaitingJob("checkExcerptStatusActivity");

    addExpectedVariable("signAttestationExcerptActivity_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_EXCERPT_ID,
        "d564f2ab-eec6-11eb-9efa-0a580a820439");
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Витяг сформовано");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptGenerationFailedTest() throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .response("/json/citizen-attestation-excerpt/data-factory/searchSubjectsResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("subjectId1")
        .response("/json/citizen-attestation-excerpt/data-factory/subjectResponse.json")
        .build());

    stubSearchSubjects("/xml/citizen-attestation-excerpt/searchSubjectsActiveResponse.xml");

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratory1")
        .response("/json/citizen-attestation-excerpt/data-factory/laboratoryResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", "laboratory1"))
        .response(
            "/json/citizen-attestation-excerpt/data-factory/lastLaboratorySolutionAddResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addApplicationResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addSolutionTypeResponse.json")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-attestation-excerpt/dso/systemSignatureRequest.json")
        .response("{\"signature\": \"signature\"}")
        .build());

    stubExcerptServiceRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("excerpts")
        .requestBody("/json/citizen-attestation-excerpt/data-factory/generateExcerptRequest.json")
        .response("{\"excerptIdentifier\":\"d564f2ab-eec6-11eb-9efa-0a580a820439\"}")
        .build());

    stubExcerptServiceRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", TestUtils.getContent("/json/testuser2AccessToken.json")))
        .uri(UriComponentsBuilder.fromPath(EXCERPT_SERVICE_MOCK_SERVER)
            .pathSegment("excerpts", "d564f2ab-eec6-11eb-9efa-0a580a820439", "status"))
        .response("{\"status\": \"FAILED\"}")
        .build());

    var processInstanceId = startProcessInstance("citizen-attestation-excerpt", testUserToken);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("subjectId", "subjectId1");
    addExpectedVariable("subjectName", "lab owner");
    addExpectedVariable("const_dataFactoryBaseUrl", "http://localhost:8877/mock-server");

    var searchLabFormDefinitionKey = "searchLabFormActivity";

    assertWaitingActivity(processInstance, searchLabFormDefinitionKey,
        "citizen-attestation-excerpt-bp-choose-lab");

    completeTask(searchLabFormDefinitionKey, processInstanceId,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    addExpectedVariable("searchLabFormActivity_completer", testUserName);
    addExpectedVariable("solutionDate", "09-08-2021");
    addExpectedCephContent(processInstanceId, searchLabFormDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    var signAttestationExcerptDefinitionKey = "signAttestationExcerptActivity";
    addExpectedCephContent(processInstanceId, signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivityPrePopulation.json");
    assertWaitingActivity(processInstance, signAttestationExcerptDefinitionKey,
        "citizen-attestation-excerpt-bp-sign-excerpt");

    completeTask(signAttestationExcerptDefinitionKey, processInstanceId,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json");
    addExpectedCephContent(processInstanceId, signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json");
    assertThat(processInstance).isWaitingAt("checkExcerptStatusActivity");

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);

    executeWaitingJob("checkExcerptStatusActivity");
    addExpectedVariable("signAttestationExcerptActivity_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Витяг не сформовано");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptGenerationTimeoutTest() throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .response("/json/citizen-attestation-excerpt/data-factory/searchSubjectsResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("subjectId1")
        .response("/json/citizen-attestation-excerpt/data-factory/subjectResponse.json")
        .build());

    stubSearchSubjects("/xml/citizen-attestation-excerpt/searchSubjectsActiveResponse.xml");

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratory1")
        .response("/json/citizen-attestation-excerpt/data-factory/laboratoryResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", "laboratory1"))
        .response(
            "/json/citizen-attestation-excerpt/data-factory/lastLaboratorySolutionAddResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addApplicationResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addSolutionTypeResponse.json")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-attestation-excerpt/dso/systemSignatureRequest.json")
        .response("{\"signature\": \"signature\"}")
        .build());

    stubExcerptServiceRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("excerpts")
        .requestBody("/json/citizen-attestation-excerpt/data-factory/generateExcerptRequest.json")
        .response("{\"excerptIdentifier\":\"d564f2ab-eec6-11eb-9efa-0a580a820439\"}")
        .build());

    stubExcerptServiceRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", TestUtils.getContent("/json/testuser2AccessToken.json")))
        .uri(UriComponentsBuilder.fromPath(EXCERPT_SERVICE_MOCK_SERVER)
            .pathSegment("excerpts", "d564f2ab-eec6-11eb-9efa-0a580a820439", "status"))
        .response("{\"status\": \"IN_PROGRESS\"}")
        .build());

    var processInstanceId = startProcessInstance("citizen-attestation-excerpt", testUserToken);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("subjectId", "subjectId1");
    addExpectedVariable("subjectName", "lab owner");
    addExpectedVariable("const_dataFactoryBaseUrl", "http://localhost:8877/mock-server");

    var searchLabFormDefinitionKey = "searchLabFormActivity";

    assertWaitingActivity(processInstance, searchLabFormDefinitionKey,
        "citizen-attestation-excerpt-bp-choose-lab");

    completeTask(searchLabFormDefinitionKey, processInstanceId,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    addExpectedVariable("searchLabFormActivity_completer", testUserName);
    addExpectedVariable("solutionDate", "09-08-2021");
    addExpectedCephContent(processInstanceId, searchLabFormDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    var signAttestationExcerptDefinitionKey = "signAttestationExcerptActivity";
    addExpectedCephContent(processInstanceId, signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivityPrePopulation.json");
    assertWaitingActivity(processInstance, signAttestationExcerptDefinitionKey,
        "citizen-attestation-excerpt-bp-sign-excerpt");

    completeTask(signAttestationExcerptDefinitionKey, processInstanceId,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json");
    addExpectedCephContent(processInstanceId, signAttestationExcerptDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json");
    assertThat(processInstance).isWaitingAt("checkExcerptStatusActivity");

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);

    assertThat(processInstance).isActive();
    executeWaitingJob("checkExcerptStatusActivity");

    executeWaitingJob("tryAgainEvent");
    assertThat(processInstance).isActive();

    executeWaitingJob("timeOutEvent");
    addExpectedVariable("signAttestationExcerptActivity_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Витяг не сформовано");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptApplicationNotAddedTest() throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .response("/json/citizen-attestation-excerpt/data-factory/searchSubjectsResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("subjectId1")
        .response("/json/citizen-attestation-excerpt/data-factory/subjectResponse.json")
        .build());

    stubSearchSubjects("/xml/citizen-attestation-excerpt/searchSubjectsActiveResponse.xml");

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratory1")
        .response("/json/citizen-attestation-excerpt/data-factory/laboratoryResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", "laboratory1"))
        .response(
            "/json/citizen-attestation-excerpt/data-factory/lastLaboratorySolutionRefuseResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addApplicationResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/citizen-attestation-excerpt/data-factory/addSolutionTypeResponse.json")
        .build());

    var processInstanceId = startProcessInstance("citizen-attestation-excerpt", testUserToken);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("subjectId", "subjectId1");
    addExpectedVariable("subjectName", "lab owner");
    addExpectedVariable("const_dataFactoryBaseUrl", "http://localhost:8877/mock-server");

    var searchLabFormDefinitionKey = "searchLabFormActivity";

    assertWaitingActivity(processInstance, searchLabFormDefinitionKey,
        "citizen-attestation-excerpt-bp-choose-lab");

    completeTask(searchLabFormDefinitionKey, processInstanceId,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    addExpectedVariable("searchLabFormActivity_completer", testUserName);
    addExpectedCephContent(processInstanceId, searchLabFormDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json");

    var noAttestationErrorDefinitionKey = "noAttestationErrorActivity";
    assertWaitingActivity(processInstance, noAttestationErrorDefinitionKey,
        "citizen-attestation-excerpt-bp-no-attestation-error");

    completeTask(noAttestationErrorDefinitionKey, processInstanceId,
        "/json/citizen-attestation-excerpt/form-data/emptyFormData.json");
    addExpectedCephContent(processInstanceId, noAttestationErrorDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/emptyFormData.json");

    addExpectedVariable("noAttestationErrorActivity_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Витяг не сформовано");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptSubjectInEdrCancelledTest() throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .response("/json/citizen-attestation-excerpt/data-factory/searchSubjectsResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("subjectId1")
        .response("/json/citizen-attestation-excerpt/data-factory/subjectResponse.json")
        .build());

    stubSearchSubjects("/xml/citizen-attestation-excerpt/searchSubjectsCancelledResponse.xml");

    var processInstanceId = startProcessInstance("citizen-attestation-excerpt", testUserToken);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("const_dataFactoryBaseUrl", "http://localhost:8877/mock-server");

    var noAttestationErrorDefinitionKey = "subjectStatusErrorActivity";
    assertWaitingActivity(processInstance, noAttestationErrorDefinitionKey,
        "citizen-attestation-excerpt-bp-status-error");

    completeTask(noAttestationErrorDefinitionKey, processInstanceId,
        "/json/citizen-attestation-excerpt/form-data/emptyFormData.json");
    addExpectedCephContent(processInstanceId, noAttestationErrorDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/emptyFormData.json");

    addExpectedVariable("subjectStatusErrorActivity_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Витяг не сформовано");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptSubjectNotFoundInEdrCancelledTest()
      throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .response("[]")
        .build());

    var processInstanceId = startProcessInstance("citizen-attestation-excerpt", testUserToken);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("const_dataFactoryBaseUrl", "http://localhost:8877/mock-server");

    var subjectNotFoundDefinitionKey = "subjectNotFoundActivity";
    assertWaitingActivity(processInstance, subjectNotFoundDefinitionKey,
        "citizen-attestation-excerpt-bp-no-subject-error");

    completeTask(subjectNotFoundDefinitionKey, processInstanceId,
        "/json/citizen-attestation-excerpt/form-data/emptyFormData.json");
    addExpectedCephContent(processInstanceId, subjectNotFoundDefinitionKey,
        "/json/citizen-attestation-excerpt/form-data/emptyFormData.json");

    addExpectedVariable("subjectNotFoundActivity_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Витяг не сформовано");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }
}
