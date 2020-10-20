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

import com.epam.digital.data.platform.bpm.it.builder.StubData;
import com.epam.digital.data.platform.bpm.it.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpm.it.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpm.it.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
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
  private static final String PROCESS_DEFINITION_KEY = "citizen-onboarding-bp";

  @Before
  public void setUp() {
    mockConnectToKeycloak(citizenRealm);
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
  @Deployment(resources = {"bpmn/citizen-onboarding-bp.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testHappyIndividualPass() throws Exception {
    var testUserToken = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(
            getClass().getResourceAsStream("/json/citizen-onboarding/indUserToken.txt"))));
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(testUserName, testUserToken));

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
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
    FormDataDto dto = new FormDataDto();
    dto.setAccessToken(testUserToken);
    var map = startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, testUserToken, dto);
    String processInstanceId = (String) map.get("id");
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("create_subject_task")
        .formKey("shared-create-subject")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-onboarding/ceph/create_subject_task_ind_prep.json"))
        .expectedVariables(
            Map.of("initiator", testUserName, "initiator_role", "unregistered-individual"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("create_subject_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-onboarding/ceph/create_subject_task_ind.json")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-onboarding/dso/indSubjectSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
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
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "INDIVIDUAL", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-onboarding/dso/subjectProfileSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-settings")
        .requestBody("/json/citizen-onboarding/data-factory/postSubjectProfileRequest.json")
        .response("{}")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("sign_subject_settings_task")
        .formKey("shared-sign-subject-settings")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-onboarding/ceph/sign_subject_setting_task_ind_prep.json"))
        .expectedVariables(Map.of("create_subject_task_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("sign_subject_settings_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-onboarding/ceph/sign_subject_setting_task_ind.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("end_process_task")
        .formKey("shared-end-process")
        .assignee(testUserName)
        .expectedVariables(Map.of("sign_subject_settings_task_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("end_process_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("{}")
        .build());

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(2);

    var subjectSystemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("subject_system_signature_ceph_key", subjectSystemSignatureCephKey);

    var subjectSettingsSystemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(1).getId() + "_system_signature_ceph_key";
    addExpectedVariable("subject_settings_system_signature_ceph_key",
        subjectSettingsSystemSignatureCephKey);

    addExpectedVariable("end_process_task_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Суб'єкт створено");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertSystemSignature(processInstanceId, "subject_system_signature_ceph_key",
        "/json/citizen-onboarding/dso/indSubjectSignatureCephContent.json");
    assertSystemSignature(processInstanceId, "subject_settings_system_signature_ceph_key",
        "/json/citizen-onboarding/dso/subjectProfileSignatureCephContent.json");
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-onboarding-bp.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testHappyEntrepreneurPass() throws Exception {
    var testUserToken = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(
            getClass().getResourceAsStream("/json/citizen-onboarding/entrUserToken.txt"))));
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(testUserName, testUserToken));

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
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
    FormDataDto dto = new FormDataDto();
    dto.setAccessToken(testUserToken);
    var map = startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, testUserToken, dto);
    String processInstanceId = (String) map.get("id");
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("create_subject_task")
        .formKey("shared-create-subject")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-onboarding/ceph/create_subject_task_entr_prep.json"))
        .expectedVariables(
            Map.of("initiator", testUserName, "initiator_role", "unregistered-entrepreneur",
                "subjectId", "subjectId"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("create_subject_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-onboarding/ceph/create_subject_task_entr.json")
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
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "ENTREPRENEUR", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-onboarding/dso/subjectProfileSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-settings")
        .requestBody("/json/citizen-onboarding/data-factory/postSubjectProfileRequest.json")
        .response("{}")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("sign_subject_settings_task")
        .formKey("shared-sign-subject-settings")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-onboarding/ceph/sign_subject_setting_task_entr_prep.json"))
        .expectedVariables(Map.of("create_subject_task_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("sign_subject_settings_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-onboarding/ceph/sign_subject_setting_task_entr.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("end_process_task")
        .formKey("shared-end-process")
        .assignee(testUserName)
        .expectedVariables(Map.of("sign_subject_settings_task_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("end_process_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("{}")
        .build());

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var subjectSettingsSystemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("subject_settings_system_signature_ceph_key",
        subjectSettingsSystemSignatureCephKey);

    addExpectedVariable("end_process_task_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Суб'єкт створено");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertSystemSignature(processInstanceId, "subject_settings_system_signature_ceph_key",
        "/json/citizen-onboarding/dso/subjectProfileSignatureCephContent.json");
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-onboarding-bp.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testHappyLegalPass() throws Exception {
    var testUserToken = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(
            getClass().getResourceAsStream("/json/citizen-onboarding/legalUserToken.txt"))));
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(testUserName, testUserToken));

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
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
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-settings-equal-settings-id")
        .queryParams(Map.of("settingsId", "c2c19401-f1b7-4954-a230-ab15566e7318"))
        .response("[]")
        .build());

    FormDataDto dto = new FormDataDto();
    dto.setAccessToken(testUserToken);
    var map = startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, testUserToken, dto);
    String processInstanceId = (String) map.get("id");
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
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
        .processInstanceId(processInstanceId)
        .activityDefinitionId("create_subject_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-onboarding/ceph/create_subject_task_legal.json")
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
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .queryParams(Map.of("subjectType", "INDIVIDUAL", "subjectCode", "1010101010"))
        .response("/json/citizen-onboarding/data-factory/searchSubjectResponse.json")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-onboarding/dso/subjectProfileSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("subject-settings")
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-onboarding/data-factory/postSubjectProfileRequest.json")
        .response("{}")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("sign_subject_settings_task")
        .formKey("shared-sign-subject-settings")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-onboarding/ceph/sign_subject_setting_task_legal_prep.json"))
        .expectedVariables(Map.of("create_subject_task_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("sign_subject_settings_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-onboarding/ceph/sign_subject_setting_task_legal.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("end_process_task")
        .formKey("shared-end-process")
        .assignee(testUserName)
        .expectedVariables(Map.of("sign_subject_settings_task_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("end_process_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("{}")
        .build());

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var subjectSettingsSystemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("subject_settings_system_signature_ceph_key",
        subjectSettingsSystemSignatureCephKey);

    addExpectedVariable("end_process_task_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Суб'єкт створено");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);

    assertSystemSignature(processInstanceId, "subject_settings_system_signature_ceph_key",
        "/json/citizen-onboarding/dso/subjectProfileSignatureCephContent.json");
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-onboarding-bp.bpmn"})
  public void testSubjectAlreadyExistFlow() throws Exception {
    var testUserName = "testuser";
    var testUserToken = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(getClass()
            .getResourceAsStream("/json/citizen-onboarding/entrUserToken.txt"))));
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(testUserName, testUserToken));

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
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
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-settings-equal-settings-id")
        .queryParams(Map.of("settingsId", "c2c19401-f1b7-4954-a230-ab15566e7318"))
        .response("[{}]")
        .build());

    var processInstanceId = startProcessInstance(PROCESS_DEFINITION_KEY, testUserToken);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("error_logout_task")
        .formKey("shared-error-logout")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("error_logout_task")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-onboarding/ceph/create_subject_task_legal.json")
        .build());

    addExpectedVariable("error_logout_task_completer", testUserName);

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }
}
