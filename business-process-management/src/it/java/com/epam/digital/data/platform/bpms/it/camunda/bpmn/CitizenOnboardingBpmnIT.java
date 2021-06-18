package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.google.common.io.ByteStreams;
import java.util.Map;
import java.util.Objects;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class CitizenOnboardingBpmnIT extends BaseBpmnIT {

  private static final String TEST_USER_NAME = "testuser";

  @Before
  public void setUp() {
    mockConnectToKeycloak();
    mockKeycloakGetUsers(TEST_USER_NAME, "/json/citizen-onboarding/keycloak/usersResponse.json");
    mockKeycloakGetRole("unregistered-individual",
        "/json/citizen-onboarding/keycloak/unregistered-individual.json", 200);
    mockKeycloakGetRole("individual", "/json/citizen-onboarding/keycloak/individual.json", 200);
    mockKeycloakGetRole("unregistered-entrepreneur",
        "/json/citizen-onboarding/keycloak/unregistered-entrepreneur.json", 200);
    mockKeycloakGetRole("entrepreneur", "/json/citizen-onboarding/keycloak/entrepreneur.json", 200);
    mockKeycloakGetRole("unregistered-legal",
        "/json/citizen-onboarding/keycloak/unregistered-legal.json", 200);
    mockKeycloakGetRole("legal", "/json/citizen-onboarding/keycloak/legal.json", 200);
    mockKeycloakDeleteRole("7004ebde-68cf-4e25-bb76-b1642a3814e4",
        "/json/citizen-onboarding/keycloak/deleteUnregisteredIndividualRequest.json");
    mockKeycloakDeleteRole("7004ebde-68cf-4e25-bb76-b1642a3814e4",
        "/json/citizen-onboarding/keycloak/deleteUnregisteredEntrepreneurRequest.json");
    mockKeycloakDeleteRole("7004ebde-68cf-4e25-bb76-b1642a3814e4",
        "/json/citizen-onboarding/keycloak/deleteUnregisteredLegalRequest.json");
    mockKeycloakAddRole("7004ebde-68cf-4e25-bb76-b1642a3814e4",
        "/json/citizen-onboarding/keycloak/postIndividualRequest.json");
    mockKeycloakAddRole("7004ebde-68cf-4e25-bb76-b1642a3814e4",
        "/json/citizen-onboarding/keycloak/postEntrepreneurRequest.json");
    mockKeycloakAddRole("7004ebde-68cf-4e25-bb76-b1642a3814e4",
        "/json/citizen-onboarding/keycloak/postLegalRequest.json");
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-onboarding-bp.bpmn"})
  public void testHappyIndividualPass() throws Exception {
    var testUserToken = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(
            getClass().getResourceAsStream("/json/citizen-onboarding/indUserToken.txt"))));

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "INDIVIDUAL", "subjectCode", "1010101010"))
        .response("[]")
        .build());

    stubSettingsRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("settings")
        .headers(Map.of("X-Access-Token", testUserToken))
        .response("/json/citizen-onboarding/data-factory/getSettingsResponse.json")
        .build());

    var processInstanceId = startProcessInstance("citizen-onboarding-bp", testUserToken);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    addExpectedVariable("initiator", TEST_USER_NAME);
    addExpectedVariable("initiator_role", "unregistered-individual");
    addExpectedVariable("const_dataFactoryBaseUrl", "http://localhost:8877/mock-server");

    var createSubjectTaskDefinitionKey = "create_subject_task";
    addExpectedCephContent(processInstanceId, "initiator_token_saving",
        "/json/citizen-onboarding/ceph/initiator_ind_token_saving.json");
    addExpectedCephContent(processInstanceId, createSubjectTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/create_subject_task_ind_prep.json");

    assertWaitingActivity(processInstance, createSubjectTaskDefinitionKey, "shared-create-subject");

    completeTask(createSubjectTaskDefinitionKey, processInstanceId,
        "/json/citizen-onboarding/ceph/create_subject_task_ind.json");

    var signSubjectSettingsTaskDefinitionKey = "sign_subject_settings_task";
    addExpectedCephContent(processInstanceId, createSubjectTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/create_subject_task_ind.json");
    addExpectedCephContent(processInstanceId, signSubjectSettingsTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_ind_prep.json");

    assertWaitingActivity(processInstance, signSubjectSettingsTaskDefinitionKey,
        "shared-sign-subject-settings");

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody("/json/citizen-onboarding/dso/indSubjectSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("subject")
        .requestBody("/json/citizen-onboarding/data-factory/postIndSubjectRequest.json")
        .response("{}")
        .build());

    stubSettingsRequest(StubData.builder()
        .resource("settings")
        .httpMethod(HttpMethod.PUT)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-onboarding/data-factory/putSettingsRequest.json")
        .response("/json/citizen-onboarding/data-factory/putSettingsResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "INDIVIDUAL", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody("/json/citizen-onboarding/dso/subjectProfileSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("subject-profile")
        .requestBody("/json/citizen-onboarding/data-factory/postSubjectProfileRequest.json")
        .response("{}")
        .build());

    completeTask(signSubjectSettingsTaskDefinitionKey, processInstanceId,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_ind.json");

    addExpectedCephContent(processInstanceId, signSubjectSettingsTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_ind.json");

    var subjectSystemSignatureCephKeyRefVarName = "subject_system_signature_ceph_key";
    var subjectSystemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        subjectSystemSignatureCephKeyRefVarName;

    var subjectSignature = cephService.getContent(cephBucketName, subjectSystemSignatureCephKey);
    var signatureMap = objectMapper.readerForMapOf(Object.class).readValue(subjectSignature);
    var expectedSignatureMap = objectMapper.readerForMapOf(Object.class).readValue(TestUtils
        .getContent("/json/citizen-onboarding/dso/indSubjectSignatureCephContent.json"));
    Assertions.assertThat(signatureMap).isEqualTo(expectedSignatureMap);

    var subjectSettingsSystemSignatureCephKeyRefVarName = "subject_settings_system_signature_ceph_key";
    var subjectSettingsSystemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        subjectSettingsSystemSignatureCephKeyRefVarName;

    var subjectSettingsSignature = cephService
        .getContent(cephBucketName, subjectSettingsSystemSignatureCephKey);
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

    assertWaitingActivity(processInstance, "end_process_task", "shared-end-process");
    completeTask("end_process_task", processInstanceId, "{}");
    addExpectedVariable("sys-var-process-completion-result", "Суб'єкт створено");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-onboarding-bp.bpmn"})
  public void testHappyEntrepreneurPass() throws Exception {
    var testUserToken = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(
            getClass().getResourceAsStream("/json/citizen-onboarding/entrUserToken.txt"))));

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "ENTREPRENEUR", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());

    stubSettingsRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("settings")
        .headers(Map.of("X-Access-Token", testUserToken))
        .response("/json/citizen-onboarding/data-factory/getNullSettingsResponse.json")
        .build());

    var processInstanceId = startProcessInstance("citizen-onboarding-bp", testUserToken);

    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    addExpectedVariable("initiator", TEST_USER_NAME);
    addExpectedVariable("initiator_role", "unregistered-entrepreneur");
    addExpectedVariable("subjectId", "subjectId");
    addExpectedVariable("const_dataFactoryBaseUrl", "http://localhost:8877/mock-server");

    var createSubjectTaskDefinitionKey = "create_subject_task";
    addExpectedCephContent(processInstanceId, "initiator_token_saving",
        "/json/citizen-onboarding/ceph/initiator_entr_token_saving.json");
    addExpectedCephContent(processInstanceId, createSubjectTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/create_subject_task_entr_prep.json");

    assertWaitingActivity(processInstance, createSubjectTaskDefinitionKey, "shared-create-subject");

    completeTask(createSubjectTaskDefinitionKey, processInstanceId,
        "/json/citizen-onboarding/ceph/create_subject_task_entr.json");

    var signSubjectSettingsTaskDefinitionKey = "sign_subject_settings_task";
    addExpectedCephContent(processInstanceId, createSubjectTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/create_subject_task_entr.json");
    addExpectedCephContent(processInstanceId, signSubjectSettingsTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_entr_prep.json");

    assertWaitingActivity(processInstance, signSubjectSettingsTaskDefinitionKey,
        "shared-sign-subject-settings");

    stubSettingsRequest(StubData.builder()
        .resource("settings")
        .httpMethod(HttpMethod.PUT)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-onboarding/data-factory/putSettingsRequest.json")
        .response("/json/citizen-onboarding/data-factory/putSettingsResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "ENTREPRENEUR", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody("/json/citizen-onboarding/dso/subjectProfileSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("subject-profile")
        .requestBody("/json/citizen-onboarding/data-factory/postSubjectProfileRequest.json")
        .response("{}")
        .build());

    completeTask(signSubjectSettingsTaskDefinitionKey, processInstanceId,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_entr.json");

    addExpectedCephContent(processInstanceId, signSubjectSettingsTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_entr.json");

    var subjectSettingsSystemSignatureCephKeyRefVarName = "subject_settings_system_signature_ceph_key";
    var subjectSettingsSystemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        subjectSettingsSystemSignatureCephKeyRefVarName;

    var subjectSettingsSignature = cephService
        .getContent(cephBucketName, subjectSettingsSystemSignatureCephKey);
    var subjectSettingsSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(subjectSettingsSignature);
    var expectedSubjectSettingsSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(TestUtils.getContent(
            "/json/citizen-onboarding/dso/subjectProfileSignatureCephContent.json"));
    Assertions.assertThat(subjectSettingsSignatureMap)
        .isEqualTo(expectedSubjectSettingsSignatureMap);

    addExpectedVariable(subjectSettingsSystemSignatureCephKeyRefVarName,
        subjectSettingsSystemSignatureCephKey);

    assertWaitingActivity(processInstance, "end_process_task", "shared-end-process");
    completeTask("end_process_task", processInstanceId, "{}");
    addExpectedVariable("sys-var-process-completion-result", "Суб'єкт створено");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-onboarding-bp.bpmn"})
  public void testHappyLegalPass() throws Exception {
    var testUserToken = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(
            getClass().getResourceAsStream("/json/citizen-onboarding/legalUserToken.txt"))));

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());

    stubSettingsRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("settings")
        .headers(Map.of("X-Access-Token", testUserToken))
        .response("/json/citizen-onboarding/data-factory/getSettingsResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-profile-sc")
        .queryParams(Map.of("settingsId", "settingsId"))
        .response("[]")
        .build());

    var processInstanceId = startProcessInstance("citizen-onboarding-bp", testUserToken);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    addExpectedVariable("initiator", TEST_USER_NAME);
    addExpectedVariable("initiator_role", "unregistered-legal");
    addExpectedVariable("subjectId", "subjectId");
    addExpectedVariable("const_dataFactoryBaseUrl", "http://localhost:8877/mock-server");

    var createSubjectTaskDefinitionKey = "create_subject_task";
    addExpectedCephContent(processInstanceId, "initiator_token_saving",
        "/json/citizen-onboarding/ceph/initiator_legal_token_saving.json");
    addExpectedCephContent(processInstanceId, createSubjectTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/create_subject_task_legal_prep.json");

    assertWaitingActivity(processInstance, createSubjectTaskDefinitionKey, "shared-create-subject");

    completeTask(createSubjectTaskDefinitionKey, processInstanceId,
        "/json/citizen-onboarding/ceph/create_subject_task_legal.json");

    var signSubjectSettingsTaskDefinitionKey = "sign_subject_settings_task";
    addExpectedCephContent(processInstanceId, createSubjectTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/create_subject_task_legal.json");
    addExpectedCephContent(processInstanceId, signSubjectSettingsTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_legal_prep.json");

    assertWaitingActivity(processInstance, signSubjectSettingsTaskDefinitionKey,
        "shared-sign-subject-settings");

    stubSettingsRequest(StubData.builder()
        .resource("settings")
        .httpMethod(HttpMethod.PUT)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-onboarding/data-factory/putSettingsRequest.json")
        .response("/json/citizen-onboarding/data-factory/putSettingsResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "INDIVIDUAL", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody("/json/citizen-onboarding/dso/subjectProfileSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("subject-profile")
        .requestBody("/json/citizen-onboarding/data-factory/postSubjectProfileRequest.json")
        .response("{}")
        .build());

    completeTask(signSubjectSettingsTaskDefinitionKey, processInstanceId,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_legal.json");

    addExpectedCephContent(processInstanceId, signSubjectSettingsTaskDefinitionKey,
        "/json/citizen-onboarding/ceph/sign_subject_setting_task_legal.json");

    var subjectSettingsSystemSignatureCephKeyRefVarName = "subject_settings_system_signature_ceph_key";
    var subjectSettingsSystemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        subjectSettingsSystemSignatureCephKeyRefVarName;

    var subjectSettingsSignature = cephService
        .getContent(cephBucketName, subjectSettingsSystemSignatureCephKey);
    var subjectSettingsSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(subjectSettingsSignature);
    var expectedSubjectSettingsSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(TestUtils.getContent(
            "/json/citizen-onboarding/dso/subjectProfileSignatureCephContent.json"));
    Assertions.assertThat(subjectSettingsSignatureMap)
        .isEqualTo(expectedSubjectSettingsSignatureMap);

    addExpectedVariable(subjectSettingsSystemSignatureCephKeyRefVarName,
        subjectSettingsSystemSignatureCephKey);

    assertWaitingActivity(processInstance, "end_process_task", "shared-end-process");
    completeTask("end_process_task", processInstanceId, "{}");
    addExpectedVariable("sys-var-process-completion-result", "Суб'єкт створено");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();
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

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "ENTREPRENEUR", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());

    stubSettingsRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("settings")
        .headers(Map.of("X-Access-Token", testUserToken))
        .response("/json/citizen-onboarding/data-factory/getSettingsResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-profile-sc")
        .queryParams(Map.of("settingsId", "settingsId"))
        .response("[{}]")
        .build());

    var processInstanceId = startProcessInstance("citizen-onboarding-bp", testUserToken);

    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("initiator_role", "unregistered-entrepreneur");
    addExpectedVariable("const_dataFactoryBaseUrl", "http://localhost:8877/mock-server");

    addExpectedCephContent(processInstanceId, "initiator_token_saving",
        "/json/citizen-onboarding/ceph/initiator_entr_token_saving.json");

    assertWaitingActivity(processInstance, "error_logout_task", "shared-error-logout");
  }
}
