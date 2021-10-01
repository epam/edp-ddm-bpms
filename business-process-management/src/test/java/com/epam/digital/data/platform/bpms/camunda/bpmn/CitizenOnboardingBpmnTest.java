package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.historyService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.bpms.camunda.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpms.camunda.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
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

  private static final String PROCESS_DEFINITION_KEY = "citizen-onboarding-bp";

  @Test
  @Deployment(resources = {"bpmn/citizen-onboarding-bp.bpmn", "bpmn/system-signature-bp.bpmn"})
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
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "INDIVIDUAL", "subjectCode", "1010101010"))
        .response("[]")
        .build());

    startProcessInstance(PROCESS_DEFINITION_KEY, Map.of("initiator", testUserName));

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("create_subject_task")
        .formKey("shared-create-subject")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-onboarding/ceph/create_subject_task_ind_prep.json"))
        .expectedVariables(
            Map.of("initiator", testUserName, "initiator_role", "unregistered-individual"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("create_subject_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-onboarding/ceph/create_subject_task_ind.json")
        .build());

    mockServer.verify();
    mockServer.reset();

    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-onboarding/dso/indSubjectSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
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
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "INDIVIDUAL", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());
    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-onboarding/dso/subjectProfileSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-settings")
        .requestBody("/json/citizen-onboarding/data-factory/postSubjectProfileRequest.json")
        .response("{}")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("sign_subject_settings_task")
        .formKey("shared-sign-subject-settings")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-onboarding/ceph/sign_subject_setting_task_ind_prep.json"))
        .expectedVariables(Map.of("create_subject_task_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("sign_subject_settings_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-onboarding/ceph/sign_subject_setting_task_ind.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("end_process_task")
        .formKey("shared-end-process")
        .assignee(testUserName)
        .expectedVariables(Map.of("sign_subject_settings_task_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("end_process_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("{}")
        .build());

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(currentProcessInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(2);

    var subjectSystemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    var subjectSettingsSystemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        processInstances.get(1).getId() + "_system_signature_ceph_key";

    addExpectedVariable("sign_subject_settings_task_completer", "testuser");
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Суб'єкт створено");
    addExpectedVariable("subject_system_signature_ceph_key", subjectSystemSignatureCephKey);
    addExpectedVariable("subject_settings_system_signature_ceph_key",
        subjectSettingsSystemSignatureCephKey);

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);

    assertSystemSignature("subject_system_signature_ceph_key",
        "/json/citizen-onboarding/dso/indSubjectSignatureCephContent.json");
    assertSystemSignature("subject_settings_system_signature_ceph_key",
        "/json/citizen-onboarding/dso/subjectProfileSignatureCephContent.json");

    verify(keycloakAddRoleConnectorDelegate).execute(any());
    verify(keycloakRemoveRoleConnectorDelegate).execute(any());
    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-onboarding-bp.bpmn", "bpmn/system-signature-bp.bpmn"})
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
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "ENTREPRENEUR", "subjectCode", "1010101010"))
        .response("[]")
        .build());

    startProcessInstance(PROCESS_DEFINITION_KEY, Map.of("initiator", testUserName));

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("create_subject_task")
        .formKey("shared-create-subject")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-onboarding/ceph/create_subject_task_entr_prep.json"))
        .expectedVariables(
            Map.of("initiator", testUserName, "initiator_role", "unregistered-entrepreneur"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("create_subject_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-onboarding/ceph/create_subject_task_entr.json")
        .build());

    mockServer.verify();
    mockServer.reset();

    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-onboarding/dso/entrSubjectSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
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
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "ENTREPRENEUR", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());
    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-onboarding/dso/subjectProfileSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-settings")
        .requestBody("/json/citizen-onboarding/data-factory/postSubjectProfileRequest.json")
        .response("{}")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("sign_subject_settings_task")
        .formKey("shared-sign-subject-settings")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-onboarding/ceph/sign_subject_setting_task_entr_prep.json"))
        .expectedVariables(Map.of("create_subject_task_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("sign_subject_settings_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-onboarding/ceph/sign_subject_setting_task_entr.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("end_process_task")
        .formKey("shared-end-process")
        .assignee(testUserName)
        .expectedVariables(Map.of("sign_subject_settings_task_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("end_process_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("{}")
        .build());

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(currentProcessInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(2);

    var subjectSystemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    var subjectSettingsSystemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        processInstances.get(1).getId() + "_system_signature_ceph_key";

    addExpectedVariable("subject_system_signature_ceph_key", subjectSystemSignatureCephKey);
    addExpectedVariable("subject_settings_system_signature_ceph_key",
        subjectSettingsSystemSignatureCephKey);
    addExpectedVariable("sign_subject_settings_task_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Суб'єкт створено");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);

    assertSystemSignature("subject_system_signature_ceph_key",
        "/json/citizen-onboarding/dso/entrSubjectSignatureCephContent.json");
    assertSystemSignature("subject_settings_system_signature_ceph_key",
        "/json/citizen-onboarding/dso/subjectProfileSignatureCephContent.json");

    verify(keycloakAddRoleConnectorDelegate).execute(any());
    verify(keycloakRemoveRoleConnectorDelegate).execute(any());
    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-onboarding-bp.bpmn", "bpmn/system-signature-bp.bpmn"})
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
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-settings-equal-settings-id")
        .queryParams(Map.of("settingsId", "settingsId"))
        .response("[]")
        .build());

    startProcessInstance(PROCESS_DEFINITION_KEY, Map.of("initiator", testUserName));

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("create_subject_task")
        .formKey("shared-create-subject")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-onboarding/ceph/create_subject_task_legal_prep.json"))
        .expectedVariables(
            Map.of("initiator", testUserName, "initiator_role", "unregistered-legal",
                "subjectId", "subjectId"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("create_subject_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-onboarding/ceph/create_subject_task_legal.json")
        .build());

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
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());
    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-onboarding/dso/subjectProfileSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-settings")
        .requestBody("/json/citizen-onboarding/data-factory/postSubjectProfileRequest.json")
        .response("{}")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("sign_subject_settings_task")
        .formKey("shared-sign-subject-settings")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-onboarding/ceph/sign_subject_setting_task_legal_prep.json"))
        .expectedVariables(Map.of("create_subject_task_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("sign_subject_settings_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-onboarding/ceph/sign_subject_setting_task_legal.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("end_process_task")
        .formKey("shared-end-process")
        .assignee(testUserName)
        .expectedVariables(Map.of("sign_subject_settings_task_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("end_process_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("{}")
        .build());

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(currentProcessInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var subjectSettingsSystemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("subject_settings_system_signature_ceph_key",
        subjectSettingsSystemSignatureCephKey);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Суб'єкт створено");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);

    assertSystemSignature("subject_settings_system_signature_ceph_key",
        "/json/citizen-onboarding/dso/subjectProfileSignatureCephContent.json");

    verify(keycloakAddRoleConnectorDelegate).execute(any());
    verify(keycloakRemoveRoleConnectorDelegate).execute(any());
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
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "ENTREPRENEUR", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-settings-equal-settings-id")
        .queryParams(Map.of("settingsId", "settingsId"))
        .response("[{}]")
        .build());

    startProcessInstance("citizen-onboarding-bp", Map.of("initiator", testUserName));

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("error_logout_task")
        .formKey("shared-error-logout")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("error_logout_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-onboarding/ceph/create_subject_task_legal.json")
        .build());

    addExpectedVariable("error_logout_task_completer", testUserName);

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }
}
