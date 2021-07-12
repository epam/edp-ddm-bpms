package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.google.common.io.ByteStreams;
import java.util.Map;
import java.util.Objects;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class CitizenOnboardingBpmnTest extends BaseBpmnTest {

  @Test
  @Deployment(resources = {"bpmn/citizen-onboarding-bp.bpmn"})
  public void testHappyIndividualPass() throws Exception {
    var testUserName = "testuser";
    var testUserToken = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(
            getClass().getResourceAsStream("/json/citizen-onboarding/indUserToken.txt"))));

    var auth = new UsernamePasswordAuthenticationToken(testUserName, testUserToken);
    SecurityContextHolder.getContext().setAuthentication(auth);

    mockSettingsRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("settings")
        .headers(Map.of("X-Access-Token", testUserToken))
        .response("/json/citizen-onboarding/data-factory/getSettingsResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "INDIVIDUAL", "subjectCode", "1010101010"))
        .response("[]")
        .build());

    startProcessInstance("citizen-onboarding-bp", Map.of("initiator", testUserName));

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("initiator_role", "unregistered-individual");

    var createSubjectTaskDefinitionKey = "create_subject_task";
    addExpectedCephContent("initiator_token_saving",
        "/json/citizen-onboarding/ceph/initiator_ind_token_saving.json");
    addExpectedCephContent(createSubjectTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/create_subject_task_ind_prep.json");

    assertWaitingActivity(createSubjectTaskDefinitionKey, "shared-create-subject");

    completeTask(createSubjectTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/create_subject_task_ind.json");

    var signSubjectSettingsTaskDefinitionKey = "sign_subject_settings_task";
    addExpectedCephContent(createSubjectTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/create_subject_task_ind.json");
    addExpectedCephContent(signSubjectSettingsTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_ind_prep.json");

    assertWaitingActivity(signSubjectSettingsTaskDefinitionKey,
        "shared-sign-subject-settings");

    mockServer.verify();
    mockServer.reset();

    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody("/json/citizen-onboarding/dso/indSubjectSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("subject")
        .requestBody("/json/citizen-onboarding/data-factory/postIndSubjectRequest.json")
        .response("{}")
        .build());

    mockSettingsRequest(StubData.builder()
        .resource("settings")
        .httpMethod(HttpMethod.PUT)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-onboarding/data-factory/putSettingsRequest.json")
        .response("/json/citizen-onboarding/data-factory/putSettingsResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "INDIVIDUAL", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());

    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody("/json/citizen-onboarding/dso/subjectProfileSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("subject-profile")
        .requestBody("/json/citizen-onboarding/data-factory/postSubjectProfileRequest.json")
        .response("{}")
        .build());

    completeTask(signSubjectSettingsTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_ind.json");

    verify(keycloakAddRoleConnectorDelegate).execute(any());
    verify(keycloakRemoveRoleConnectorDelegate).execute(any());

    addExpectedCephContent(signSubjectSettingsTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_ind.json");

    var subjectSystemSignatureCephKeyRefVarName = "subject_system_signature_ceph_key";
    var subjectSystemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        subjectSystemSignatureCephKeyRefVarName;

    var subjectSignature = cephService.getContent(cephBucketName, subjectSystemSignatureCephKey)
        .get();
    var signatureMap = objectMapper.readerForMapOf(Object.class).readValue(subjectSignature);
    var expectedSignatureMap = objectMapper.readerForMapOf(Object.class).readValue(TestUtils
        .getContent("/json/citizen-onboarding/dso/indSubjectSignatureCephContent.json"));
    Assertions.assertThat(signatureMap).isEqualTo(expectedSignatureMap);

    var subjectSettingsSystemSignatureCephKeyRefVarName = "subject_settings_system_signature_ceph_key";
    var subjectSettingsSystemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        subjectSettingsSystemSignatureCephKeyRefVarName;

    var subjectSettingsSignature = cephService
        .getContent(cephBucketName, subjectSettingsSystemSignatureCephKey).get();
    var subjectSettingsSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(subjectSettingsSignature);
    var expectedSubjectSettingsSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(TestUtils.getContent(
            "/json/citizen-onboarding/dso/subjectProfileSignatureCephContent.json"));
    Assertions.assertThat(subjectSettingsSignatureMap)
        .isEqualTo(expectedSubjectSettingsSignatureMap);

    addExpectedVariable(subjectSettingsSystemSignatureCephKeyRefVarName,
        subjectSettingsSystemSignatureCephKey);
    addExpectedVariable(subjectSystemSignatureCephKeyRefVarName, subjectSystemSignatureCephKey);

    assertWaitingActivity("end_process_task", "shared-end-process");
    completeTask("end_process_task", "{}");
    addExpectedVariable("sys-var-process-completion-result", "Суб'єкт створено");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();

    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-onboarding-bp.bpmn"})
  public void testHappyEntrepreneurPass() throws Exception {
    var testUserName = "testuser";
    var testUserToken = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(
            getClass().getResourceAsStream("/json/citizen-onboarding/entrUserToken.txt"))));

    var auth = new UsernamePasswordAuthenticationToken(testUserName, testUserToken);
    SecurityContextHolder.getContext().setAuthentication(auth);

    mockSettingsRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("settings")
        .headers(Map.of("X-Access-Token", testUserToken))
        .response("/json/citizen-onboarding/data-factory/getSettingsResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "ENTREPRENEUR", "subjectCode", "1010101010"))
        .response("[]")
        .build());

    startProcessInstance("citizen-onboarding-bp", Map.of("initiator", testUserName));

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("initiator_role", "unregistered-entrepreneur");

    var createSubjectTaskDefinitionKey = "create_subject_task";
    addExpectedCephContent("initiator_token_saving",
        "/json/citizen-onboarding/ceph/initiator_entr_token_saving.json");
    addExpectedCephContent(createSubjectTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/create_subject_task_entr_prep.json");

    assertWaitingActivity(createSubjectTaskDefinitionKey, "shared-create-subject");

    completeTask(createSubjectTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/create_subject_task_entr.json");

    var signSubjectSettingsTaskDefinitionKey = "sign_subject_settings_task";
    addExpectedCephContent(createSubjectTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/create_subject_task_entr.json");
    addExpectedCephContent(signSubjectSettingsTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_entr_prep.json");

    assertWaitingActivity(signSubjectSettingsTaskDefinitionKey,
        "shared-sign-subject-settings");

    mockServer.verify();
    mockServer.reset();

    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody("/json/citizen-onboarding/dso/entrSubjectSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("subject")
        .requestBody("/json/citizen-onboarding/data-factory/postEntrSubjectRequest.json")
        .response("{}")
        .build());

    mockSettingsRequest(StubData.builder()
        .resource("settings")
        .httpMethod(HttpMethod.PUT)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-onboarding/data-factory/putSettingsRequest.json")
        .response("/json/citizen-onboarding/data-factory/putSettingsResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "ENTREPRENEUR", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());

    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody("/json/citizen-onboarding/dso/subjectProfileSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("subject-profile")
        .requestBody("/json/citizen-onboarding/data-factory/postSubjectProfileRequest.json")
        .response("{}")
        .build());

    completeTask(signSubjectSettingsTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_entr.json");

    verify(keycloakAddRoleConnectorDelegate).execute(any());
    verify(keycloakRemoveRoleConnectorDelegate).execute(any());

    addExpectedCephContent(signSubjectSettingsTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_entr.json");

    var subjectSystemSignatureCephKeyRefVarName = "subject_system_signature_ceph_key";
    var subjectSystemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        subjectSystemSignatureCephKeyRefVarName;

    var subjectSignature = cephService.getContent(cephBucketName, subjectSystemSignatureCephKey)
        .get();
    var signatureMap = objectMapper.readerForMapOf(Object.class).readValue(subjectSignature);
    var expectedSignatureMap = objectMapper.readerForMapOf(Object.class).readValue(TestUtils
        .getContent("/json/citizen-onboarding/dso/entrSubjectSignatureCephContent.json"));
    Assertions.assertThat(signatureMap).isEqualTo(expectedSignatureMap);

    var subjectSettingsSystemSignatureCephKeyRefVarName = "subject_settings_system_signature_ceph_key";
    var subjectSettingsSystemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        subjectSettingsSystemSignatureCephKeyRefVarName;

    var subjectSettingsSignature = cephService
        .getContent(cephBucketName, subjectSettingsSystemSignatureCephKey).get();
    var subjectSettingsSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(subjectSettingsSignature);
    var expectedSubjectSettingsSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(TestUtils.getContent(
            "/json/citizen-onboarding/dso/subjectProfileSignatureCephContent.json"));
    Assertions.assertThat(subjectSettingsSignatureMap)
        .isEqualTo(expectedSubjectSettingsSignatureMap);

    addExpectedVariable(subjectSettingsSystemSignatureCephKeyRefVarName,
        subjectSettingsSystemSignatureCephKey);
    addExpectedVariable(subjectSystemSignatureCephKeyRefVarName, subjectSystemSignatureCephKey);

    assertWaitingActivity("end_process_task", "shared-end-process");
    completeTask("end_process_task", "{}");
    addExpectedVariable("sys-var-process-completion-result", "Суб'єкт створено");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();

    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-onboarding-bp.bpmn"})
  public void testHappyLegalPass() throws Exception {
    var testUserName = "testuser";
    var testUserToken = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(
            getClass().getResourceAsStream("/json/citizen-onboarding/legalUserToken.txt"))));

    var auth = new UsernamePasswordAuthenticationToken(testUserName, testUserToken);
    SecurityContextHolder.getContext().setAuthentication(auth);

    mockSettingsRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("settings")
        .headers(Map.of("X-Access-Token", testUserToken))
        .response("/json/citizen-onboarding/data-factory/getSettingsResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-profile-sc")
        .queryParams(Map.of("settingsId", "settingsId"))
        .response("[]")
        .build());

    startProcessInstance("citizen-onboarding-bp", Map.of("initiator", testUserName));

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("initiator_role", "unregistered-legal");
    addExpectedVariable("subjectId", "subjectId");

    var createSubjectTaskDefinitionKey = "create_subject_task";
    addExpectedCephContent("initiator_token_saving",
        "/json/citizen-onboarding/ceph/initiator_legal_token_saving.json");
    addExpectedCephContent(createSubjectTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/create_subject_task_legal_prep.json");

    assertWaitingActivity(createSubjectTaskDefinitionKey, "shared-create-subject");

    completeTask(createSubjectTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/create_subject_task_legal.json");

    var signSubjectSettingsTaskDefinitionKey = "sign_subject_settings_task";
    addExpectedCephContent(createSubjectTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/create_subject_task_legal.json");
    addExpectedCephContent(signSubjectSettingsTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_legal_prep.json");

    assertWaitingActivity(signSubjectSettingsTaskDefinitionKey,
        "shared-sign-subject-settings");

    mockServer.verify();
    mockServer.reset();

    mockSettingsRequest(StubData.builder()
        .resource("settings")
        .httpMethod(HttpMethod.PUT)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-onboarding/data-factory/putSettingsRequest.json")
        .response("/json/citizen-onboarding/data-factory/putSettingsResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());

    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody("/json/citizen-onboarding/dso/subjectProfileSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("subject-profile")
        .requestBody("/json/citizen-onboarding/data-factory/postSubjectProfileRequest.json")
        .response("{}")
        .build());

    completeTask(signSubjectSettingsTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_legal.json");

    verify(keycloakAddRoleConnectorDelegate).execute(any());
    verify(keycloakRemoveRoleConnectorDelegate).execute(any());

    addExpectedCephContent(signSubjectSettingsTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_legal.json");

    var subjectSettingsSystemSignatureCephKeyRefVarName = "subject_settings_system_signature_ceph_key";
    var subjectSettingsSystemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        subjectSettingsSystemSignatureCephKeyRefVarName;

    var subjectSettingsSignature = cephService
        .getContent(cephBucketName, subjectSettingsSystemSignatureCephKey).get();
    var subjectSettingsSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(subjectSettingsSignature);
    var expectedSubjectSettingsSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(TestUtils.getContent(
            "/json/citizen-onboarding/dso/subjectProfileSignatureCephContent.json"));
    Assertions.assertThat(subjectSettingsSignatureMap)
        .isEqualTo(expectedSubjectSettingsSignatureMap);

    addExpectedVariable(subjectSettingsSystemSignatureCephKeyRefVarName,
        subjectSettingsSystemSignatureCephKey);

    assertWaitingActivity("end_process_task", "shared-end-process");
    completeTask("end_process_task", "{}");
    addExpectedVariable("sys-var-process-completion-result", "Суб'єкт створено");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();

    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-onboarding-bp.bpmn"})
  public void testSubjectAlreadyExistFlow() throws Exception {
    var testUserName = "testuser";
    var testUserToken = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(getClass()
            .getResourceAsStream("/json/citizen-onboarding/entrUserToken.txt"))));

    var auth = new UsernamePasswordAuthenticationToken(testUserName, testUserToken);
    SecurityContextHolder.getContext().setAuthentication(auth);

    mockSettingsRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("settings")
        .headers(Map.of("X-Access-Token", testUserToken))
        .response("/json/citizen-onboarding/data-factory/getSettingsResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "ENTREPRENEUR", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-profile-sc")
        .queryParams(Map.of("settingsId", "settingsId"))
        .response("[{}]")
        .build());

    startProcessInstance("citizen-onboarding-bp", Map.of("initiator", testUserName));

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("initiator_role", "unregistered-entrepreneur");

    addExpectedCephContent("initiator_token_saving",
        "/json/citizen-onboarding/ceph/initiator_entr_token_saving.json");

    assertWaitingActivity("error_logout_task", "shared-error-logout");
  }
}
