package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.google.common.io.ByteStreams;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;

@Deployment(resources = {"bpmn/officer-create-subject-bp.bpmn"})
public class OfficerCreateSubjectIT extends BaseBpmnIT {

  @Test
  public void testHappyPath() throws Exception {
    var subjectType = "LEGAL";
    var subjectCode = "10101010";
    var testUserToken = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(
            getClass().getResourceAsStream("/json/officer-create-subject/legalUserToken.txt"))));

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", subjectType, "subjectCode", subjectCode))
        .headers(Map.of("X-Access-Token", testUserToken))
        .response("[]")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody("/json/officer-create-subject/dso/subjectSystemSignatureRequest.json")
        .response("{\"signature\": \"userSignature\"}")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("subject")
        .requestBody("/json/officer-create-subject/data-factory/postSubjectRequest.json")
        .response("{}")
        .build());

    stubSearchSubjects("/xml/officer-create-subject/searchSubjectsResponse.xml");

    var startFormCephKey = "startFormCephKey";
    var data = new LinkedHashMap<String, Object>();
    data.put("subjectType", subjectType);
    data.put("edrpou", subjectCode);
    cephService.putFormData(startFormCephKey, FormDataDto.builder().data(data).build());

    var processInstanceId = startProcessInstanceWithStartFormAndGetId("officer-create-subject-bp",
        "startFormCephKey", testUserToken);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    addExpectedVariable("initiator", "testuser");
    addExpectedVariable("const_dataFactoryBaseUrl", "http://localhost:8877/mock-server");
    addExpectedVariable("start_form_ceph_key", startFormCephKey);

    var signSubjectTaskDefinitionKey = "sign_subject_officer_create_subject_task";
    assertWaitingActivity(processInstance, signSubjectTaskDefinitionKey,
        "sign-subject-officer-create-subject-bp");

    completeTask(signSubjectTaskDefinitionKey, processInstanceId,
        "/json/officer-create-subject/ceph/sign-subject-officer-create-task.json");

    addCompleterUsernameVariable(signSubjectTaskDefinitionKey, null);
    addExpectedCephContent(processInstanceId, signSubjectTaskDefinitionKey,
        "/json/officer-create-subject/ceph/sign-subject-officer-create-task.json");

    var subjectSettingsSystemSignatureCephKeyRefVarName = "subject_system_signature_ceph_key";
    var subjectSettingsSystemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        subjectSettingsSystemSignatureCephKeyRefVarName;

    var subjectSettingsSignature = cephService
        .getContent(cephBucketName, subjectSettingsSystemSignatureCephKey).get();
    var subjectSettingsSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(subjectSettingsSignature);
    var expectedSubjectSettingsSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(TestUtils.getContent(
            "/json/officer-create-subject/dso/subjectSignatureCephContent.json"));
    Assertions.assertThat(subjectSettingsSignatureMap)
        .isEqualTo(expectedSubjectSettingsSignatureMap);

    addExpectedVariable(subjectSettingsSystemSignatureCephKeyRefVarName,
        subjectSettingsSystemSignatureCephKey);

    addExpectedVariable("sys-var-process-completion-result", "Суб'єкт створено");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();
  }

  @Test
  public void testIndividualSubjectTypeHappyPath() throws Exception {
    var subjectName = "testSubjectName";
    var subjectType = "INDIVIDUAL";
    var subjectCode = "1010101010";
    var testUserToken = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(
            getClass().getResourceAsStream("/json/officer-create-subject/indUserToken.txt"))));

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", subjectType, "subjectCode", subjectCode))
        .headers(Map.of("X-Access-Token", testUserToken))
        .response("[]")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody(
            "/json/officer-create-subject/dso/individual/subjectSystemSignatureRequest.json")
        .response("{\"signature\": \"userSignature\"}")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("subject")
        .requestBody("/json/officer-create-subject/data-factory/individual/postSubjectRequest.json")
        .response("{}")
        .build());

    var startFormCephKey = "startFormCephKey";
    var data = new LinkedHashMap<String, Object>();
    data.put("subjectName", subjectName);
    data.put("subjectType", subjectType);
    data.put("rnokppCode", subjectCode);
    cephService.putFormData(startFormCephKey, FormDataDto.builder().data(data).build());

    var processInstanceId = startProcessInstanceWithStartFormAndGetId("officer-create-subject-bp",
        "startFormCephKey", testUserToken);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    addExpectedVariable("initiator", "testuser");
    addExpectedVariable("const_dataFactoryBaseUrl", "http://localhost:8877/mock-server");
    addExpectedVariable("start_form_ceph_key", startFormCephKey);

    var signSubjectTaskDefinitionKey = "sign_subject_officer_create_subject_task";
    assertWaitingActivity(processInstance, signSubjectTaskDefinitionKey,
        "sign-subject-officer-create-subject-bp");

    completeTask(signSubjectTaskDefinitionKey, processInstanceId,
        "/json/officer-create-subject/ceph/individual/sign-subject-officer-create-task.json");

    addCompleterUsernameVariable(signSubjectTaskDefinitionKey, null);
    addExpectedCephContent(processInstanceId, signSubjectTaskDefinitionKey,
        "/json/officer-create-subject/ceph/individual/sign-subject-officer-create-task.json");

    var subjectSettingsSystemSignatureCephKeyRefVarName = "subject_system_signature_ceph_key";
    var subjectSettingsSystemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        subjectSettingsSystemSignatureCephKeyRefVarName;

    var subjectSettingsSignature = cephService
        .getContent(cephBucketName, subjectSettingsSystemSignatureCephKey).get();
    var subjectSettingsSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(subjectSettingsSignature);
    var expectedSubjectSettingsSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(TestUtils.getContent(
            "/json/officer-create-subject/dso/individual/subjectSignatureCephContent.json"));
    Assertions.assertThat(subjectSettingsSignatureMap)
        .isEqualTo(expectedSubjectSettingsSignatureMap);

    addExpectedVariable(subjectSettingsSystemSignatureCephKeyRefVarName,
        subjectSettingsSystemSignatureCephKey);

    addExpectedVariable("sys-var-process-completion-result", "Суб'єкт створено");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();
  }

  @Test
  public void testValidationErrorSubjectCreated() throws Exception {
    var subjectType = "LEGAL";
    var subjectCode = "1010101010";
    var testUserToken = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(
            getClass().getResourceAsStream("/json/officer-create-subject/legalUserToken.txt"))));

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", subjectType, "subjectCode", subjectCode))
        .headers(Map.of("X-Access-Token", testUserToken))
        .response(
            "/json/officer-create-subject/data-factory/subjectEqualSubjectTypeEqualSubjectCodeExistResponse.json")
        .build());

    var startFormCephKey = "startFormCephKey";
    var data = new LinkedHashMap<String, Object>();
    data.put("subjectType", subjectType);
    data.put("edrpou", subjectCode);
    cephService.putFormData(startFormCephKey, FormDataDto.builder().data(data).build());

    var response = startProcessInstanceWithStartForm("officer-create-subject-bp",
        "startFormCephKey", testUserToken);

    assertNotNull(response);
    assertEquals(response.get("message"), "Validation error");
    var errors = (List<Map>) ((Map) response.get("details")).get("errors");
    assertEquals(2, errors.size());
    assertEquals("Такий суб'єкт вже існує", errors.get(0).get("message"));
    assertEquals("subjectType", errors.get(0).get("field"));
    assertEquals(subjectType, errors.get(0).get("value"));
    assertEquals("Такий суб'єкт вже існує", errors.get(1).get("message"));
    assertEquals("subjectCode", errors.get(1).get("field"));
    assertEquals(subjectCode, errors.get(1).get("value"));
  }

  @Test
  public void testValidationErrorSubjectHasCanceledOrSuspendedState() throws Exception {
    var subjectType = "LEGAL";
    var subjectCode = "1010101010";
    var testUserToken = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(
            getClass().getResourceAsStream("/json/officer-create-subject/legalUserToken.txt"))));

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", subjectType, "subjectCode", subjectCode))
        .headers(Map.of("X-Access-Token", testUserToken))
        .response("[]")
        .build());

    stubSearchSubjects("/xml/officer-create-subject/searchSubjectsSuspendedStateResponse.xml");

    var startFormCephKey = "startFormCephKey";
    var data = new LinkedHashMap<String, Object>();
    data.put("subjectType", subjectType);
    data.put("edrpou", subjectCode);
    cephService.putFormData(startFormCephKey, FormDataDto.builder().data(data).build());

    var response = startProcessInstanceWithStartForm("officer-create-subject-bp",
        "startFormCephKey", testUserToken);

    assertNotNull(response);
    assertEquals(response.get("message"), "Validation error");
    var errors = (List<Map>) ((Map) response.get("details")).get("errors");
    assertEquals(1, errors.size());
    assertEquals("Статус суб'єкту скасовано або припинено", errors.get(0).get("message"));
    assertEquals("subjectCode", errors.get(0).get("field"));
    assertEquals(subjectCode, errors.get(0).get("value"));
  }

}
