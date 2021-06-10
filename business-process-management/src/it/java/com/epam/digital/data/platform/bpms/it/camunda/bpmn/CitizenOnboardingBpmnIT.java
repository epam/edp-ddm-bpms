package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.google.common.io.ByteStreams;
import java.io.IOException;
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
  public void setUp() throws IOException {
    mockConnectToKeycloak();
    mockKeycloakGetUsers(TEST_USER_NAME,
        "/json/create-subject-ind-entrp/keycloak/usersResponse.json");
    mockKeycloakGetRole("unregistered-individual",
        "/json/create-subject-ind-entrp/keycloak/unregistered-individual.json", 200);
    mockKeycloakGetRole("individual", "/json/create-subject-ind-entrp/keycloak/individual.json",
        200);
    mockKeycloakGetRole("unregistered-entrepreneur",
        "/json/create-subject-ind-entrp/keycloak/unregistered-entrepreneur.json", 200);
    mockKeycloakGetRole("entrepreneur", "/json/create-subject-ind-entrp/keycloak/entrepreneur.json",
        200);
    mockKeycloakDeleteRole("7004ebde-68cf-4e25-bb76-b1642a3814e4",
        "/json/create-subject-ind-entrp/keycloak/deleteUnregisteredIndividualRequest.json");
    mockKeycloakDeleteRole("7004ebde-68cf-4e25-bb76-b1642a3814e4",
        "/json/create-subject-ind-entrp/keycloak/deleteUnregisteredEntrepreneurRequest.json");
    mockKeycloakAddRole("7004ebde-68cf-4e25-bb76-b1642a3814e4",
        "/json/create-subject-ind-entrp/keycloak/postIndividualRequest.json");
    mockKeycloakAddRole("7004ebde-68cf-4e25-bb76-b1642a3814e4",
        "/json/create-subject-ind-entrp/keycloak/postEntrepreneurRequest.json");
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-onboarding-bp.bpmn"})
  public void testHappyIndividualPass() throws Exception {
    var testUserToken = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(
            getClass().getResourceAsStream("/json/create-subject-ind-entrp/indUserToken.txt"))));

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
        .response("/json/create-subject-ind-entrp/data-factory/getSettingsResponse.json")
        .build());

    var processInstanceId = startProcessInstance("citizen-onboarding-bp", testUserToken);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    addExpectedVariable("initiator", TEST_USER_NAME);
    addExpectedVariable("initiator_role", "unregistered-individual");
    addExpectedVariable("const_dataFactoryBaseUrl", "http://localhost:8877/mock-server");

    var createSubjectTaskDefinitionKey = "create_subject_task";
    addExpectedCephContent(processInstanceId, "initiator_token_saving",
        "/json/create-subject-ind-entrp/ceph/initiator_ind_token_saving.json");
    addExpectedCephContent(processInstanceId, createSubjectTaskDefinitionKey,
        "/json/create-subject-ind-entrp/ceph/create_subject_task_ind_prep.json");

    assertWaitingActivity(processInstance, createSubjectTaskDefinitionKey, "shared-create-subject");

    completeTask(createSubjectTaskDefinitionKey, processInstanceId,
        "/json/create-subject-ind-entrp/ceph/create_subject_task_ind.json");

    var signSubjectSettingsTaskDefinitionKey = "sign_subject_settings_task";
    addExpectedCephContent(processInstanceId, createSubjectTaskDefinitionKey,
        "/json/create-subject-ind-entrp/ceph/create_subject_task_ind.json");
    addExpectedCephContent(processInstanceId, signSubjectSettingsTaskDefinitionKey,
        "/json/create-subject-ind-entrp/ceph/sign_subject_setting_task_ind_prep.json");

    assertWaitingActivity(processInstance, signSubjectSettingsTaskDefinitionKey,
        "shared-sign-subject-settings");

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody("/json/create-subject-ind-entrp/dso/indSubjectSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("subject")
        .requestBody("/json/create-subject-ind-entrp/data-factory/postIndSubjectRequest.json")
        .response("{}")
        .build());

    stubSettingsRequest(StubData.builder()
        .resource("settings")
        .httpMethod(HttpMethod.PUT)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-subject-ind-entrp/data-factory/putSettingsRequest.json")
        .response("/json/create-subject-ind-entrp/data-factory/putSettingsResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "INDIVIDUAL", "subjectCode", "1010101010"))
        .response("/json/create-subject-ind-entrp/data-factory/searchSubjectResponse.json")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody("/json/create-subject-ind-entrp/dso/subjectProfileSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("subject-profile")
        .requestBody("/json/create-subject-ind-entrp/data-factory/postSubjectProfileRequest.json")
        .response("{}")
        .build());

    completeTask(signSubjectSettingsTaskDefinitionKey, processInstanceId,
        "/json/create-subject-ind-entrp/ceph/sign_subject_setting_task_ind.json");

    addExpectedCephContent(processInstanceId, signSubjectSettingsTaskDefinitionKey,
        "/json/create-subject-ind-entrp/ceph/sign_subject_setting_task_ind.json");

    var subjectSystemSignatureCephKeyRefVarName = "subject_system_signature_ceph_key";
    var subjectSystemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        subjectSystemSignatureCephKeyRefVarName;

    var subjectSignature = cephService.getContent(cephBucketName, subjectSystemSignatureCephKey);
    var signatureMap = objectMapper.readerForMapOf(Object.class).readValue(subjectSignature);
    var expectedSignatureMap = objectMapper.readerForMapOf(Object.class).readValue(TestUtils
        .getContent("/json/create-subject-ind-entrp/dso/indSubjectSignatureCephContent.json"));
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
            "/json/create-subject-ind-entrp/dso/subjectProfileSignatureCephContent.json"));
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
            getClass().getResourceAsStream("/json/create-subject-ind-entrp/entrUserToken.txt"))));

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "ENTREPRENEUR", "subjectCode", "1010101010"))
        .response("[]")
        .build());

    stubSettingsRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("settings")
        .headers(Map.of("X-Access-Token", testUserToken))
        .response("/json/create-subject-ind-entrp/data-factory/getSettingsResponse.json")
        .build());

    var processInstanceId = startProcessInstance("citizen-onboarding-bp", testUserToken);

    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    addExpectedVariable("initiator", TEST_USER_NAME);
    addExpectedVariable("initiator_role", "unregistered-entrepreneur");
    addExpectedVariable("const_dataFactoryBaseUrl", "http://localhost:8877/mock-server");

    var createSubjectTaskDefinitionKey = "create_subject_task";
    addExpectedCephContent(processInstanceId, "initiator_token_saving",
        "/json/create-subject-ind-entrp/ceph/initiator_entr_token_saving.json");
    addExpectedCephContent(processInstanceId, createSubjectTaskDefinitionKey,
        "/json/create-subject-ind-entrp/ceph/create_subject_task_entr_prep.json");

    assertWaitingActivity(processInstance, createSubjectTaskDefinitionKey, "shared-create-subject");

    completeTask(createSubjectTaskDefinitionKey, processInstanceId,
        "/json/create-subject-ind-entrp/ceph/create_subject_task_entr.json");

    var signSubjectSettingsTaskDefinitionKey = "sign_subject_settings_task";
    addExpectedCephContent(processInstanceId, createSubjectTaskDefinitionKey,
        "/json/create-subject-ind-entrp/ceph/create_subject_task_entr.json");
    addExpectedCephContent(processInstanceId, signSubjectSettingsTaskDefinitionKey,
        "/json/create-subject-ind-entrp/ceph/sign_subject_setting_task_entr_prep.json");

    assertWaitingActivity(processInstance, signSubjectSettingsTaskDefinitionKey,
        "shared-sign-subject-settings");

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody("/json/create-subject-ind-entrp/dso/entrSubjectSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("subject")
        .requestBody("/json/create-subject-ind-entrp/data-factory/postEntrSubjectRequest.json")
        .response("{}")
        .build());

    stubSettingsRequest(StubData.builder()
        .resource("settings")
        .httpMethod(HttpMethod.PUT)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-subject-ind-entrp/data-factory/putSettingsRequest.json")
        .response("/json/create-subject-ind-entrp/data-factory/putSettingsResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "ENTREPRENEUR", "subjectCode", "1010101010"))
        .response("/json/create-subject-ind-entrp/data-factory/searchSubjectResponse.json")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody("/json/create-subject-ind-entrp/dso/subjectProfileSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("subject-profile")
        .requestBody("/json/create-subject-ind-entrp/data-factory/postSubjectProfileRequest.json")
        .response("{}")
        .build());

    completeTask(signSubjectSettingsTaskDefinitionKey, processInstanceId,
        "/json/create-subject-ind-entrp/ceph/sign_subject_setting_task_entr.json");

    addExpectedCephContent(processInstanceId, signSubjectSettingsTaskDefinitionKey,
        "/json/create-subject-ind-entrp/ceph/sign_subject_setting_task_entr.json");

    var subjectSystemSignatureCephKeyRefVarName = "subject_system_signature_ceph_key";
    var subjectSystemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        subjectSystemSignatureCephKeyRefVarName;

    var subjectSignature = cephService.getContent(cephBucketName, subjectSystemSignatureCephKey);
    var signatureMap = objectMapper.readerForMapOf(Object.class).readValue(subjectSignature);
    var expectedSignatureMap = objectMapper.readerForMapOf(Object.class).readValue(TestUtils
        .getContent("/json/create-subject-ind-entrp/dso/entrSubjectSignatureCephContent.json"));
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
            "/json/create-subject-ind-entrp/dso/subjectProfileSignatureCephContent.json"));
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
  public void testSubjectAlreadyExistFlow() throws Exception {
    var testUserName = "testuser";
    var testUserToken = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(getClass()
            .getResourceAsStream("/json/create-subject-ind-entrp/entrUserToken.txt"))));

    var auth = new UsernamePasswordAuthenticationToken(testUserName, testUserToken);
    SecurityContextHolder.getContext().setAuthentication(auth);

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "ENTREPRENEUR", "subjectCode", "1010101010"))
        .response("/json/create-subject-ind-entrp/data-factory/searchSubjectResponse.json")
        .build());

    var processInstanceId = startProcessInstance("citizen-onboarding-bp", testUserToken);

    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("initiator_role", "unregistered-entrepreneur");
    addExpectedVariable("const_dataFactoryBaseUrl", "http://localhost:8877/mock-server");

    addExpectedCephContent(processInstanceId, "initiator_token_saving",
        "/json/create-subject-ind-entrp/ceph/initiator_entr_token_saving.json");

    assertWaitingActivity(processInstance, "error_logout_task", "shared-error-logout");
  }
}
