package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.historyService;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorDetailDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;

public class CreateAppExpanseBpmnIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_ID = "create-app-expanse";

  @Value("${camunda.system-variables.const_dataFactoryBaseUrl}")
  private String dataFactoryBaseUrl;

  @Test
  @Deployment(resources = {"bpmn/create-app-expanse.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testAdditionHappyPass_accreditationFlagIsTrue() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    var addApplicationActivityDefinitionKey = "Activity_shared-add-application";
    var addFactorsActivityDefinitionKey = "Activity_shared-add-factors";
    var checkComplianceActivityDefinitionKey = "Activity_shared-check-complience";
    var addDecisionIncludeActivityDefinitionKey = "Activity_shared-add-decision-include";
    var addLetterDataActivityDefinitionKey = "Activity_shared-add-letter-data";
    var signAppIncludeActivityDefinitionKey = "Activity_shared-sign-app-include";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());

    stubSearchSubjects("/xml/create-app/searchSubjectsActiveResponse.xml");

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/create-app/data-factory/last-laboratory-solution-add.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/solutionTypeAddResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/applicationTypeResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "EXPANSE"))
        .response("/json/create-app/data-factory/applicationTypeExpanseResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app/data-factory/findLaboratoryResponse.json")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-app/dso/expanseIncludeSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("registration")
        .requestBody("/json/create-app/data-factory/createApplicationExpanseRequest.json")
        .response("{}")
        .build());

    var processInstanceId = startProcessInstanceAndGetId(labId);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    expectedVariablesMap.put("initiator", testUserName);
    expectedVariablesMap.put("fullName", "testuser testuser testuser");
    expectedVariablesMap.put("const_dataFactoryBaseUrl", dataFactoryBaseUrl);
    expectedVariablesMap.put("start_form_ceph_key", START_FORM_CEPH_KEY);

    // add application
    assertWaitingActivity(processInstance, addApplicationActivityDefinitionKey,
        "shared-add-application");

    completeTask(addApplicationActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-application.json");

    addCompleterUsernameVariable(addApplicationActivityDefinitionKey, testUserName);
    addExpectedCephContent(processInstanceId, addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-application.json");
    addExpectedCephContent(processInstanceId, addFactorsActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");
    // add bio phys labor and chem factors
    assertWaitingActivity(processInstance, addFactorsActivityDefinitionKey, "shared-add-factors");

    completeTask(addFactorsActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-factors.json");

    addCompleterUsernameVariable(addFactorsActivityDefinitionKey, testUserName);
    addExpectedCephContent(processInstanceId, addFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-factors.json");
    addExpectedCephContent(processInstanceId, checkComplianceActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");

    // check compliance
    assertWaitingActivity(processInstance, checkComplianceActivityDefinitionKey,
        "shared-check-complience");

    completeTask(checkComplianceActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-check-compliance_noErrors.json");

    addCompleterUsernameVariable(checkComplianceActivityDefinitionKey, testUserName);
    addExpectedCephContent(processInstanceId, checkComplianceActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-check-compliance_noErrors.json");
    addExpectedCephContent(processInstanceId, addDecisionIncludeActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-solution-include-prepopulation.json");
    expectedVariablesMap.put("solutionTypeId", "ADD=SUCCESS");
    // add decision include
    assertWaitingActivity(processInstance, addDecisionIncludeActivityDefinitionKey,
        "shared-add-decision-include");

    completeTask(addDecisionIncludeActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-decision-include.json");

    addCompleterUsernameVariable(addDecisionIncludeActivityDefinitionKey, testUserName);
    addExpectedCephContent(processInstanceId, addDecisionIncludeActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-decision-include.json");
    addExpectedCephContent(processInstanceId, addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-solution-include-prepopulation.json");

    // add letter data
    assertWaitingActivity(processInstance, addLetterDataActivityDefinitionKey,
        "shared-add-letter-data");

    completeTask(addLetterDataActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-letter-data.json");

    addCompleterUsernameVariable(addLetterDataActivityDefinitionKey, testUserName);
    addExpectedCephContent(processInstanceId, addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-letter-data.json");
    addExpectedCephContent(processInstanceId, signAppIncludeActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-include_prepopulation.json");

    // sign app include
    assertWaitingActivity(processInstance, signAppIncludeActivityDefinitionKey,
        "shared-sign-app-include");

    completeTask(signAppIncludeActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-sign-app-include.json");

    addCompleterUsernameVariable(signAppIncludeActivityDefinitionKey, testUserName);
    addExpectedCephContent(processInstanceId, signAppIncludeActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-include.json");

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);

    addExpectedVariable("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
    addExpectedVariable("sys-var-process-completion-result",
        "Прийнято рішення про розширення факторів");

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/create-app/dso/expanseIncludeSystemSignatureCephContent.json");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();
  }

  @Test
  @Deployment(resources = {"bpmn/create-app-expanse.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testDenyingHappyPass_accreditationFlagIsTrue() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    var subjectStatusErrorActivityDefinitionKey = "Activity_shared-subject-status-error";
    var addApplicationActivityDefinitionKey = "Activity_shared-add-application";
    var addFactorsActivityDefinitionKey = "Activity_shared-add-factors";
    var checkComplianceActivityDefinitionKey = "Activity_shared-check-complience";
    var addDecisionDenyActivityDefinitionKey = "Activity_shared-add-decision-deny";
    var addLetterDataActivityDefinitionKey = "Activity_1eujure";
    var signAppDenyActivityDefinitionKey = "Activity_shared-sign-app-deny";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());

    stubSearchSubjects("/xml/create-app/searchSubjectsDisabledResponse.xml");

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app/data-factory/findLaboratoryResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "WO_CONSIDER"))
        .response("/json/create-app/data-factory/solutionTypeWoConsiderResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "EXPANSE"))
        .response("/json/create-app/data-factory/applicationTypeExpanseResponse.json")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-app/dso/expanseDenySystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("registration")
        .requestBody("/json/create-app/data-factory/createApplicationExpanseDenyRequest.json")
        .response("{}")
        .build());

    var processInstanceId = startProcessInstanceAndGetId(labId);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    expectedVariablesMap.put("initiator", testUserName);
    expectedVariablesMap.put("const_dataFactoryBaseUrl", dataFactoryBaseUrl);
    expectedVariablesMap.put("start_form_ceph_key", START_FORM_CEPH_KEY);

    // subject status error
    assertWaitingActivity(processInstance, subjectStatusErrorActivityDefinitionKey,
        "shared-subject-status-error");
    completeTask(subjectStatusErrorActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-subject-status-error.json");
    addExpectedCephContent(processInstanceId, subjectStatusErrorActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-subject-status-error.json");
    addCompleterUsernameVariable(subjectStatusErrorActivityDefinitionKey, testUserName);
    expectedVariablesMap.put("edrSuspendedOrCancelled", "true");
    expectedVariablesMap.put("fullName", null);
    // add application
    assertWaitingActivity(processInstance, addApplicationActivityDefinitionKey,
        "shared-add-application");

    completeTask(addApplicationActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-application.json");

    addCompleterUsernameVariable(addApplicationActivityDefinitionKey, testUserName);
    addExpectedCephContent(processInstanceId, addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-application.json");
    addExpectedCephContent(processInstanceId, addFactorsActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");
    // add bio phys labor and chem factors
    assertWaitingActivity(processInstance, addFactorsActivityDefinitionKey,
        "shared-add-factors");

    completeTask(addFactorsActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-factors.json");

    addCompleterUsernameVariable(addFactorsActivityDefinitionKey, testUserName);
    addExpectedCephContent(processInstanceId, addFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-factors.json");
    addExpectedCephContent(processInstanceId, checkComplianceActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");

    // check compliance
    assertWaitingActivity(processInstance, checkComplianceActivityDefinitionKey,
        "shared-check-complience");

    completeTask(checkComplianceActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-check-compliance_errors.json");

    addCompleterUsernameVariable(checkComplianceActivityDefinitionKey, testUserName);
    addExpectedCephContent(processInstanceId, checkComplianceActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-check-compliance_errors.json");
    addExpectedCephContent(processInstanceId, addDecisionDenyActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-solution-deny-prepopulation.json");
    expectedVariablesMap.put("solutionTypeId", "woconsider");
    // add decision include
    assertWaitingActivity(processInstance, addDecisionDenyActivityDefinitionKey,
        "shared-add-decision-deny");

    completeTask(addDecisionDenyActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-decision-deny.json");

    addCompleterUsernameVariable(addDecisionDenyActivityDefinitionKey, testUserName);
    addExpectedCephContent(processInstanceId, addDecisionDenyActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-decision-deny.json");
    addExpectedCephContent(processInstanceId, addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-solution-deny-prepopulation.json");

    // add letter data
    assertWaitingActivity(processInstance, addLetterDataActivityDefinitionKey,
        "shared-add-letter-data");

    completeTask(addLetterDataActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-letter-data.json");

    addCompleterUsernameVariable(addLetterDataActivityDefinitionKey, testUserName);
    addExpectedCephContent(processInstanceId, addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-letter-data.json");
    addExpectedCephContent(processInstanceId, signAppDenyActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-deny_prepopulation.json");

    // sign app deny
    assertWaitingActivity(processInstance, signAppDenyActivityDefinitionKey,
        "shared-sign-app-deny");

    completeTask(signAppDenyActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-sign-app-deny.json");

    addCompleterUsernameVariable(signAppDenyActivityDefinitionKey, testUserName);
    addExpectedCephContent(processInstanceId, signAppDenyActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-deny.json");

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);

    addExpectedVariable("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
    addExpectedVariable("sys-var-process-completion-result",
        "Прийнято рішення про залишення без розгляду");

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/create-app/dso/expanseDenySystemSignatureCephContent.json");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testValidationError() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());

    stubSearchSubjects("/xml/create-app/searchSubjectsActiveResponse.xml");

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/create-app/data-factory/last-laboratory-solution-add.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/solutionTypeAddResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/applicationTypeResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response(
            "/json/create-app/data-factory/findLaboratoryWithoutAccreditationResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-equal-laboratory-id-count")
        .queryParams(Map.of("laboratoryId", labId))
        .response("[]")
        .build());

    var resultMap = startProcessInstanceForError(labId);

    var errors = resultMap.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "laboratory"),
        Map.entry("message", "Додайте кадровий склад до лабораторії"),
        Map.entry("value", labId));
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testNoAccreditationFlag() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";

    var addApplicationActivityDefinitionKey = "Activity_shared-add-application";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());

    stubSearchSubjects("/xml/create-app/searchSubjectsActiveResponse.xml");

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/create-app/data-factory/last-laboratory-solution-add.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/solutionTypeAddResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/applicationTypeResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response(
            "/json/create-app/data-factory/findLaboratoryWithoutAccreditationResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-equal-laboratory-id-count")
        .queryParams(Map.of("laboratoryId", labId))
        .response("[{\"cnt\":1}]")
        .build());

    var processInstanceId = startProcessInstanceAndGetId(labId);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    expectedVariablesMap.put("initiator", testUserName);
    expectedVariablesMap.put("fullName", "testuser testuser testuser");
    expectedVariablesMap.put("const_dataFactoryBaseUrl", dataFactoryBaseUrl);
    expectedVariablesMap.put("start_form_ceph_key", START_FORM_CEPH_KEY);
    addExpectedCephContent(processInstanceId, addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/name-edrpou-prepopulation.json");

    assertWaitingActivity(processInstance, addApplicationActivityDefinitionKey,
        "shared-add-application");
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testAppIsNotCreated() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());

    stubSearchSubjects("/xml/create-app/searchSubjectsActiveResponse.xml");

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/create-app/data-factory/last-laboratory-solution-deny.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/solutionTypeAddResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/applicationTypeResponse.json")
        .build());

    var errorMap = startProcessInstanceForError(labId);

    var errors = errorMap.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "laboratory"),
        Map.entry("message", "Заява на первинне внесення не створена"),
        Map.entry("value", labId));
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testSubjectIsDisabledButNoErrors() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";

    var subjectStatusErrorActivityDefinitionKey = "Activity_shared-subject-status-error";
    var addApplicationActivityDefinitionKey = "Activity_shared-add-application";
    var addFactorsActivityDefinitionKey = "Activity_shared-add-factors";
    var checkComplianceActivityDefinitionKey = "Activity_shared-check-complience";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());

    stubSearchSubjects("/xml/create-app/searchSubjectsDisabledResponse.xml");

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app/data-factory/findLaboratoryResponse.json")
        .build());

    var processInstanceId = startProcessInstanceAndGetId(labId);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    expectedVariablesMap.put("initiator", testUserName);
    expectedVariablesMap.put("const_dataFactoryBaseUrl", dataFactoryBaseUrl);
    expectedVariablesMap.put("start_form_ceph_key", START_FORM_CEPH_KEY);

    // subject status error
    assertWaitingActivity(processInstance, subjectStatusErrorActivityDefinitionKey,
        "shared-subject-status-error");
    completeTask(subjectStatusErrorActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-subject-status-error.json");
    addExpectedCephContent(processInstanceId, subjectStatusErrorActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-subject-status-error.json");
    addCompleterUsernameVariable(subjectStatusErrorActivityDefinitionKey, testUserName);
    expectedVariablesMap.put("edrSuspendedOrCancelled", "true");
    expectedVariablesMap.put("fullName", null);
    // add application
    assertWaitingActivity(processInstance, addApplicationActivityDefinitionKey,
        "shared-add-application");

    completeTask(addApplicationActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-application.json");

    addCompleterUsernameVariable(addApplicationActivityDefinitionKey, testUserName);
    addExpectedCephContent(processInstanceId, addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-application.json");
    addExpectedCephContent(processInstanceId, addFactorsActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");
    // add bio phys labor and chem factors
    assertWaitingActivity(processInstance, addFactorsActivityDefinitionKey,
        "shared-add-factors");

    completeTask(addFactorsActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-factors.json");

    addCompleterUsernameVariable(addFactorsActivityDefinitionKey, testUserName);
    addExpectedCephContent(processInstanceId, addFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-factors.json");
    // check compliance
    assertWaitingActivity(processInstance, checkComplianceActivityDefinitionKey,
        "shared-check-complience");

    var ex = org.junit.jupiter.api.Assertions.assertThrows(ValidationException.class,
        () -> completeTask(checkComplianceActivityDefinitionKey, processInstanceId,
            "/json/create-app/form-data/Activity_shared-check-compliance_noErrors.json"));

    Assertions.assertThat(ex.getDetails().getErrors()).hasSize(1);
    Assertions.assertThat(ex.getDetails().getErrors()).contains(
        new ErrorDetailDto("Статус суб'єкта в ЄДР \"Скаcовано\" або \"Припинено\","
            + " оберіть відповідну причину відмови", "errorCheckFlag", "false"));
  }

  private String startProcessInstanceAndGetId(String labId) throws JsonProcessingException {
    createFormData(labId);
    return startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_ID, testUserToken,
        createFormData(labId));
  }

  private FormDataDto createFormData(String labId) {
    var data = new LinkedHashMap<String, Object>();
    data.put("laboratory", Map.of("laboratoryId", labId,"subjectId", "activeSubject"));
    data.put("edrpou", "77777777");
    data.put("subjectType", "LEGAL");
    return FormDataDto.builder().data(data).build();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Map<String, List<Map<String, String>>>> startProcessInstanceForError(
      String labId) throws JsonProcessingException {
    var resultMap = startProcessInstanceWithStartForm(PROCESS_DEFINITION_ID,
        testUserToken, createFormData(labId));
    return (Map<String, Map<String, List<Map<String, String>>>>) resultMap;
  }
}
