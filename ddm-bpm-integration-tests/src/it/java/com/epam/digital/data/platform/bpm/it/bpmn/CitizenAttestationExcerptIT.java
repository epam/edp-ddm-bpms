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
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.historyService;

import com.epam.digital.data.platform.bpm.it.builder.StubData;
import com.epam.digital.data.platform.bpm.it.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpm.it.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpm.it.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpm.it.util.TestUtils;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessExcerptIdVariable;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

public class CitizenAttestationExcerptIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_KEY = "citizen-attestation-excerpt";

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
    stubSearchSubjects("/xml/citizen-attestation-excerpt/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .requestBody("{\"subjectType\":\"LEGAL\",\"subjectCode\":\"01010101\"}")
        .response("/json/citizen-attestation-excerpt/data-factory/searchSubjectsResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("subjectId1")
        .response("/json/citizen-attestation-excerpt/data-factory/subjectResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratory1")
        .response("/json/citizen-attestation-excerpt/data-factory/laboratoryResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .requestBody("{\"laboratoryId\":\"laboratory1\"}")
        .response(
            "/json/citizen-attestation-excerpt/data-factory/lastLaboratorySolutionAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
        .response("/json/citizen-attestation-excerpt/data-factory/addApplicationResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
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
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .formKey("citizen-attestation-excerpt-bp-choose-lab")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName, "subjectId", "subjectId1",
            "subjectName", "lab owner"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signAttestationExcerptActivity")
        .formKey("citizen-attestation-excerpt-bp-sign-excerpt")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivityPrePopulation.json"))
        .expectedVariables(
            Map.of("searchLabFormActivity_completer", testUserName, "solutionDate", "09-08-2021"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signAttestationExcerptActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json")
        .build());

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
    addExpectedVariable(ProcessExcerptIdVariable.SYS_VAR_PROCESS_EXCERPT_ID,
        "d564f2ab-eec6-11eb-9efa-0a580a820439");
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Витяг сформовано");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptHappyPathNoEdrTest() throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .requestBody("{\"subjectType\":\"LEGAL\",\"subjectCode\":\"01010101\"}")
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
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .requestBody("{\"laboratoryId\":\"laboratory1\"}")
        .response(
            "/json/citizen-attestation-excerpt/data-factory/lastLaboratorySolutionAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
        .response("/json/citizen-attestation-excerpt/data-factory/addApplicationResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
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
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .formKey("citizen-attestation-excerpt-bp-choose-lab")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName, "subjectId", "subjectId1",
            "subjectName", "absent subject name"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signAttestationExcerptActivity")
        .formKey("citizen-attestation-excerpt-bp-sign-excerpt")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivityPrePopulation.json"))
        .expectedVariables(
            Map.of("searchLabFormActivity_completer", testUserName, "solutionDate", "09-08-2021"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signAttestationExcerptActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json")
        .build());

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
    addExpectedVariable(ProcessExcerptIdVariable.SYS_VAR_PROCESS_EXCERPT_ID,
        "d564f2ab-eec6-11eb-9efa-0a580a820439");
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Витяг сформовано");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptGenerationFailedTest() throws JsonProcessingException {
    stubSearchSubjects("/xml/citizen-attestation-excerpt/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .requestBody("{\"subjectType\":\"LEGAL\",\"subjectCode\":\"01010101\"}")
        .response("/json/citizen-attestation-excerpt/data-factory/searchSubjectsResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("subjectId1")
        .response("/json/citizen-attestation-excerpt/data-factory/subjectResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratory1")
        .response("/json/citizen-attestation-excerpt/data-factory/laboratoryResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .requestBody("{\"laboratoryId\":\"laboratory1\"}")
        .response(
            "/json/citizen-attestation-excerpt/data-factory/lastLaboratorySolutionAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
        .response("/json/citizen-attestation-excerpt/data-factory/addApplicationResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
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
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .formKey("citizen-attestation-excerpt-bp-choose-lab")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName, "subjectId", "subjectId1",
            "subjectName", "lab owner"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signAttestationExcerptActivity")
        .formKey("citizen-attestation-excerpt-bp-sign-excerpt")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivityPrePopulation.json"))
        .expectedVariables(
            Map.of("searchLabFormActivity_completer", testUserName, "solutionDate", "09-08-2021"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signAttestationExcerptActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json")
        .build());

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
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Витяг не сформовано");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptGenerationTimeoutTest() throws JsonProcessingException {
    stubSearchSubjects("/xml/citizen-attestation-excerpt/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .requestBody("{\"subjectType\":\"LEGAL\",\"subjectCode\":\"01010101\"}")
        .response("/json/citizen-attestation-excerpt/data-factory/searchSubjectsResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("subjectId1")
        .response("/json/citizen-attestation-excerpt/data-factory/subjectResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratory1")
        .response("/json/citizen-attestation-excerpt/data-factory/laboratoryResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .requestBody("{\"laboratoryId\":\"laboratory1\"}")
        .response(
            "/json/citizen-attestation-excerpt/data-factory/lastLaboratorySolutionAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
        .response("/json/citizen-attestation-excerpt/data-factory/addApplicationResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
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
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .formKey("citizen-attestation-excerpt-bp-choose-lab")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName, "subjectId", "subjectId1",
            "subjectName", "lab owner"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signAttestationExcerptActivity")
        .formKey("citizen-attestation-excerpt-bp-sign-excerpt")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivityPrePopulation.json"))
        .expectedVariables(
            Map.of("searchLabFormActivity_completer", testUserName, "solutionDate", "09-08-2021"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signAttestationExcerptActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/signAttestationExcerptActivity.json")
        .build());

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
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Витяг не сформовано");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptApplicationNotAddedTest() throws JsonProcessingException {
    stubSearchSubjects("/xml/citizen-attestation-excerpt/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .requestBody("{\"subjectType\":\"LEGAL\",\"subjectCode\":\"01010101\"}")
        .response("/json/citizen-attestation-excerpt/data-factory/searchSubjectsResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("subjectId1")
        .response("/json/citizen-attestation-excerpt/data-factory/subjectResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratory1")
        .response("/json/citizen-attestation-excerpt/data-factory/laboratoryResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .requestBody("{\"laboratoryId\":\"laboratory1\"}")
        .response(
            "/json/citizen-attestation-excerpt/data-factory/lastLaboratorySolutionRefuseResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
        .response("/json/citizen-attestation-excerpt/data-factory/addApplicationResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .requestBody("{\"constantCode\":\"ADD\"}")
        .response("/json/citizen-attestation-excerpt/data-factory/addSolutionTypeResponse.json")
        .build());

    var processInstanceId = startProcessInstance("citizen-attestation-excerpt", testUserToken);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .formKey("citizen-attestation-excerpt-bp-choose-lab")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName, "subjectId", "subjectId1",
            "subjectName", "lab owner"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("searchLabFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/searchLabFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("noAttestationErrorActivity")
        .formKey("citizen-attestation-excerpt-bp-no-attestation-error")
        .assignee(testUserName)
        .expectedVariables(Map.of("searchLabFormActivity_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("noAttestationErrorActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-attestation-excerpt/form-data/emptyFormData.json")
        .build());

    addExpectedVariable("noAttestationErrorActivity_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Витяг не сформовано");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptSubjectInEdrCancelledTest() throws JsonProcessingException {
    stubSearchSubjects("/xml/citizen-attestation-excerpt/searchSubjectsCancelledResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .requestBody("{\"subjectType\":\"LEGAL\",\"subjectCode\":\"01010101\"}")
        .response("/json/citizen-attestation-excerpt/data-factory/searchSubjectsResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("subjectId1")
        .response("/json/citizen-attestation-excerpt/data-factory/subjectResponse.json")
        .build());

    var processInstanceId = startProcessInstance(PROCESS_DEFINITION_KEY, testUserToken);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("subjectStatusErrorActivity")
        .formKey("citizen-attestation-excerpt-bp-status-error")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("subjectStatusErrorActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-attestation-excerpt/form-data/emptyFormData.json")
        .build());

    addExpectedVariable("subjectStatusErrorActivity_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Витяг не сформовано");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-attestation-excerpt.bpmn",
      "bpmn/system-signature-bp.bpmn", "bpmn/check-excerpt-status.bpmn"})
  public void citizenAttestationExcerptSubjectNotFoundInEdrCancelledTest()
      throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .requestBody("{\"subjectType\":\"LEGAL\",\"subjectCode\":\"01010101\"}")
        .response("[]")
        .build());

    var processInstanceId = startProcessInstance("citizen-attestation-excerpt", testUserToken);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("subjectNotFoundActivity")
        .formKey("citizen-attestation-excerpt-bp-no-subject-error")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("subjectNotFoundActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-attestation-excerpt/form-data/emptyFormData.json")
        .build());

    addExpectedVariable("subjectNotFoundActivity_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Витяг не сформовано");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }
}
