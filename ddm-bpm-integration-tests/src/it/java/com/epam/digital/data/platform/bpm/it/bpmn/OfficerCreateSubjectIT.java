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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.epam.digital.data.platform.bpm.it.builder.StubData;
import com.epam.digital.data.platform.bpm.it.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpm.it.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpm.it.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@Deployment(resources = {"bpmn/officer-create-subject-bp.bpmn", "bpmn/system-signature-bp.bpmn"})
public class OfficerCreateSubjectIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_KEY = "officer-create-subject-bp";
  private static final String LEGAL_SUBJECT_TYPE = "LEGAL";
  private static final String LEGAL_SUBJECT_CODE = "10101010";
  private static final String ENTREPRENEUR_SUBJECT_TYPE = "ENTREPRENEUR";
  private static final String ENTREPRENEUR_SUBJECT_CODE = "1010101010";

  private static String legalUserToken;
  private static String indUserToken;

  @BeforeClass
  public static void setup() throws IOException {
    legalUserToken = new String(ByteStreams.toByteArray(Objects.requireNonNull(
        BaseIT.class.getResourceAsStream("/json/officer-create-subject/legalUserToken.txt"))));

    indUserToken = new String(ByteStreams.toByteArray(Objects.requireNonNull(
        BaseIT.class.getResourceAsStream("/json/officer-create-subject/indUserToken.txt"))));
  }

  @Test
  public void testHappyPath() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(testUserName, legalUserToken));

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", LEGAL_SUBJECT_TYPE, "subjectCode", LEGAL_SUBJECT_CODE))
        .headers(Map.of("X-Access-Token", legalUserToken))
        .response("[]")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", legalUserToken))
        .requestBody("/json/officer-create-subject/dso/subjectSystemSignatureRequest.json")
        .response("{\"signature\": \"userSignature\"}")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", legalUserToken))
        .resource("subject")
        .requestBody("/json/officer-create-subject/data-factory/postSubjectRequest.json")
        .response("{}")
        .build());
    stubSearchSubjects("/xml/officer-create-subject/searchSubjectsResponse.xml");

    var data = deserializeFormData("/json/officer-create-subject/ceph/start_event_legal.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        legalUserToken, data);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("sign_subject_officer_create_subject_task")
        .formKey("sign-subject-officer-create-subject-bp")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/officer-create-subject/ceph/sign-subject-officer-create-task.json"))
        .expectedVariables(Map.of("initiator", testUserName,
            StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("sign_subject_officer_create_subject_task")
        .completerUserName(testUserName)
        .completerAccessToken(legalUserToken)
        .expectedFormData(
            "/json/officer-create-subject/ceph/sign-subject-officer-create-task.json")
        .build());

    addExpectedVariable("sign_subject_officer_create_subject_task_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Суб'єкт створено");

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("subject_system_signature_ceph_key", systemSignatureCephKey);

    assertSystemSignature(processInstanceId, "subject_system_signature_ceph_key",
        "/json/officer-create-subject/dso/subjectSignatureCephContent.json");
    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  public void testEntrepreneurSubjectTypeHappyPath() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(testUserName, indUserToken));

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", ENTREPRENEUR_SUBJECT_TYPE, "subjectCode",
            ENTREPRENEUR_SUBJECT_CODE))
        .headers(Map.of("X-Access-Token", indUserToken))
        .response("[]")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", indUserToken))
        .requestBody(
            "/json/officer-create-subject/dso/entrepreneur2/subjectSystemSignatureRequest.json")
        .response("{\"signature\": \"userSignature\"}")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", indUserToken))
        .resource("subject")
        .requestBody(
            "/json/officer-create-subject/data-factory/entrepreneur2/postSubjectRequest.json")
        .response("{}")
        .build());
    stubSearchSubjects("/xml/officer-create-subject/searchSubjectsEmptyResponse.xml");

    var data = deserializeFormData(
        "/json/officer-create-subject/ceph/start_event_entrepreneur.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        indUserToken, data);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("sign_subject_officer_create_subject_task")
        .formKey("sign-subject-officer-create-subject-bp")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/officer-create-subject/ceph/entrepreneur2/sign-subject-officer-create-task.json"))
        .expectedVariables(Map.of("initiator", testUserName,
            StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("sign_subject_officer_create_subject_task")
        .completerUserName(testUserName)
        .completerAccessToken(indUserToken)
        .expectedFormData(
            "/json/officer-create-subject/ceph/entrepreneur2/sign-subject-officer-create-task.json")
        .build());

    addExpectedVariable("sign_subject_officer_create_subject_task_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Суб'єкт створено");

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("subject_system_signature_ceph_key", systemSignatureCephKey);

    assertSystemSignature(processInstanceId, "subject_system_signature_ceph_key",
        "/json/officer-create-subject/dso/entrepreneur2/subjectSignatureCephContent.json");
    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  public void testValidationErrorSubjectCreated() throws Exception {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", LEGAL_SUBJECT_TYPE, "subjectCode", LEGAL_SUBJECT_CODE))
        .headers(Map.of("X-Access-Token", legalUserToken))
        .response(
            "/json/officer-create-subject/data-factory/subjectEqualSubjectTypeEqualSubjectCodeExistResponse.json")
        .build());

    var data = deserializeFormData("/json/officer-create-subject/ceph/start_event_legal.json");
    var response = startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, legalUserToken, data);

    assertNotNull(response);
    assertNotNull(response.get("message"));
    var errors = (List<Map>) ((Map) response.get("details")).get("errors");
    assertEquals(2, errors.size());
    assertEquals("Такий суб'єкт вже існує", errors.get(0).get("message"));
    assertEquals("subjectType", errors.get(0).get("field"));
    assertEquals(LEGAL_SUBJECT_TYPE, errors.get(0).get("value"));
    assertEquals("Такий суб'єкт вже існує", errors.get(1).get("message"));
    assertEquals("subjectCode", errors.get(1).get("field"));
    assertEquals(LEGAL_SUBJECT_CODE, errors.get(1).get("value"));
  }

  @Test
  public void testValidationErrorSubjectHasCanceledOrSuspendedState() throws Exception {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", LEGAL_SUBJECT_TYPE, "subjectCode", LEGAL_SUBJECT_CODE))
        .headers(Map.of("X-Access-Token", legalUserToken))
        .response("[]")
        .build());
    stubSearchSubjects("/xml/officer-create-subject/searchSubjectsSuspendedStateResponse.xml");

    var data = deserializeFormData("/json/officer-create-subject/ceph/start_event_legal.json");
    var response = startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, legalUserToken, data);

    assertNotNull(response);
    assertNotNull(response.get("message"));
    var errors = (List<Map>) ((Map) response.get("details")).get("errors");
    assertEquals(1, errors.size());
    assertEquals("Статус суб'єкту скасовано або припинено", errors.get(0).get("message"));
    assertEquals("subjectCode", errors.get(0).get("field"));
    assertEquals(LEGAL_SUBJECT_CODE, errors.get(0).get("value"));
  }

  @Test
  public void testValidationErrorSubjectNotFound() throws IOException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", LEGAL_SUBJECT_TYPE, "subjectCode", LEGAL_SUBJECT_CODE))
        .headers(Map.of("X-Access-Token", legalUserToken))
        .response("[]")
        .build());
    stubSearchSubjects("/xml/officer-create-subject/searchSubjectsEmptyResponse.xml");

    var data = deserializeFormData("/json/officer-create-subject/ceph/start_event_legal.json");
    var response = startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, legalUserToken, data);

    assertNotNull(response);
    assertNotNull(response.get("message"));
    var errors = (List<Map>) ((Map) response.get("details")).get("errors");
    assertEquals(1, errors.size());
    assertEquals("Суб'єкта немає в ЄДР", errors.get(0).get("message"));
    assertEquals("subjectCode", errors.get(0).get("field"));
    assertEquals(LEGAL_SUBJECT_CODE, errors.get(0).get("value"));
  }

  @Test
  public void testAbsentEdrFlagTrueEdrResponseNotEmpty() throws IOException {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(testUserName, indUserToken));

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", ENTREPRENEUR_SUBJECT_TYPE, "subjectCode",
            ENTREPRENEUR_SUBJECT_CODE))
        .headers(Map.of("X-Access-Token", indUserToken))
        .response("[]")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", indUserToken))
        .requestBody(
            "/json/officer-create-subject/dso/entrepreneur/subjectSystemSignatureRequest.json")
        .response("{\"signature\": \"userSignature\"}")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", indUserToken))
        .resource("subject")
        .requestBody(
            "/json/officer-create-subject/data-factory/entrepreneur/postSubjectRequest.json")
        .response("{}")
        .build());
    stubSearchSubjects("/xml/officer-create-subject/searchSubjectsEntrepreneurResponse.xml");

    var data = deserializeFormData(
        "/json/officer-create-subject/ceph/start_event_entrepreneur.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        indUserToken, data);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("sign_subject_officer_create_subject_task")
        .formKey("sign-subject-officer-create-subject-bp")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/officer-create-subject/ceph/entrepreneur/sign-subject-officer-create-task-flag-false.json"))
        .expectedVariables(Map.of("initiator", testUserName,
            StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("sign_subject_officer_create_subject_task")
        .completerUserName(testUserName)
        .completerAccessToken(indUserToken)
        .expectedFormData(
            "/json/officer-create-subject/ceph/entrepreneur/sign-subject-officer-create-task-flag-false.json")
        .build());

    addExpectedVariable("sign_subject_officer_create_subject_task_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Суб'єкт створено");

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("subject_system_signature_ceph_key", systemSignatureCephKey);

    assertSystemSignature(processInstanceId, "subject_system_signature_ceph_key",
        "/json/officer-create-subject/dso/entrepreneur/subjectSignatureCephContent.json");
    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }
}
