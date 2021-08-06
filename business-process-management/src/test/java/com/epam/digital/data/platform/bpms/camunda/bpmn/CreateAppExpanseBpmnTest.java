package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.historyService;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorDetailDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CreateAppExpanseBpmnTest extends BaseBpmnTest {

  private static final String PROCESS_DEFINITION_ID = "create-app-expanse";
  private static final String START_FORM_CEPH_KEY = "startFormCephKey";

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

    mockEdrResponse("/json/create-app/edr/searchSubjectsActiveResponse.json");

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/create-app/data-factory/last-laboratory-solution-add.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/applicationTypeResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/solutionTypeAddResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app/data-factory/findLaboratoryResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/solutionTypeAddResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "EXPANSE"))
        .response("/json/create-app/data-factory/applicationTypeExpanseResponse.json")
        .build());

    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-app/dso/expanseIncludeSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("registration")
        .requestBody("/json/create-app/data-factory/createApplicationExpanseRequest.json")
        .response("{}")
        .build());

    startProcessInstanceWithStartForm(labId);

    expectedVariablesMap.put("initiator", testUserName);
    expectedVariablesMap.put("fullName", null);
    expectedVariablesMap.put("start_form_ceph_key", START_FORM_CEPH_KEY);

    // add application
    assertWaitingActivity(addApplicationActivityDefinitionKey, "shared-add-application");

    completeTask(addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-application.json");

    addExpectedVariable("Activity_shared-add-application_completer", testUserName);
    addExpectedCephContent(addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-application.json");
    addExpectedCephContent(addFactorsActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");
    // add bio phys labor and chem factors
    assertWaitingActivity(addFactorsActivityDefinitionKey, "shared-add-factors");

    completeTask(addFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-factors.json");

    addExpectedVariable("Activity_shared-add-factors_completer", testUserName);
    addExpectedCephContent(addFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-factors.json");
    addExpectedCephContent(checkComplianceActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");

    // check compliance
    assertWaitingActivity(checkComplianceActivityDefinitionKey, "shared-check-complience");

    completeTask(checkComplianceActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-check-compliance_noErrors.json");

    addExpectedCephContent(checkComplianceActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-check-compliance_noErrors.json");
    addExpectedCephContent(addDecisionIncludeActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-solution-include-prepopulation.json");
    addExpectedVariable("solutionTypeId", "ADD=SUCCESS");
    addExpectedVariable("Activity_shared-check-complience_completer", testUserName);
    // add decision include
    assertWaitingActivity(addDecisionIncludeActivityDefinitionKey,
        "shared-add-decision-include");

    completeTask(addDecisionIncludeActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-decision-include.json");

    addExpectedVariable("Activity_shared-add-decision-include_completer", testUserName);
    addExpectedCephContent(addDecisionIncludeActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-decision-include.json");
    addExpectedCephContent(addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-solution-include-prepopulation.json");

    // add letter data
    assertWaitingActivity(addLetterDataActivityDefinitionKey, "shared-add-letter-data");

    completeTask(addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-letter-data.json");

    addExpectedVariable("Activity_shared-add-letter-data_completer", testUserName);
    addExpectedCephContent(addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-letter-data.json");
    addExpectedCephContent(signAppIncludeActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-include_prepopulation.json");

    // sign app include
    assertWaitingActivity(signAppIncludeActivityDefinitionKey, "shared-sign-app-include");

    completeTask(signAppIncludeActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-include.json");

    addExpectedVariable("Activity_shared-sign-app-include_completer", testUserName);
    addExpectedCephContent(signAppIncludeActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-include.json");

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(currentProcessInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);

    addExpectedVariable("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
    addExpectedVariable("sys-var-process-completion-result",
        "Прийнято рішення про розширення факторів");

    assertSystemSignature("system_signature_ceph_key",
        "/json/create-app/dso/expanseIncludeSystemSignatureCephContent.json");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();

    mockServer.verify();
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

    mockEdrResponse("/json/create-app/edr/searchSubjectsDisabledResponse.json");

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app/data-factory/findLaboratoryResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "WO_CONSIDER"))
        .response("/json/create-app/data-factory/solutionTypeWoConsiderResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "EXPANSE"))
        .response("/json/create-app/data-factory/applicationTypeExpanseResponse.json")
        .build());

    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-app/dso/expanseDenySystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("registration")
        .requestBody("/json/create-app/data-factory/createApplicationExpanseDenyRequest.json")
        .response("{}")
        .build());

    startProcessInstanceWithStartForm(labId);

    expectedVariablesMap.put("initiator", testUserName);
    expectedVariablesMap.put("start_form_ceph_key", START_FORM_CEPH_KEY);

    // subject status error
    assertWaitingActivity(subjectStatusErrorActivityDefinitionKey,
        "shared-subject-status-error");
    completeTask(subjectStatusErrorActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-subject-status-error.json");
    addExpectedCephContent(subjectStatusErrorActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-subject-status-error.json");
    expectedVariablesMap.put("Activity_shared-subject-status-error_completer", testUserName);
    expectedVariablesMap.put("edrSuspendedOrCancelled", "true");
    expectedVariablesMap.put("fullName", null);
    // add application
    assertWaitingActivity(addApplicationActivityDefinitionKey, "shared-add-application");

    completeTask(addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-application.json");

    addExpectedVariable("Activity_shared-add-application_completer", testUserName);
    addExpectedCephContent(addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-application.json");
    addExpectedCephContent(addFactorsActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");
    // add bio phys labor and chem factors
    assertWaitingActivity(addFactorsActivityDefinitionKey, "shared-add-factors");

    completeTask(addFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-factors.json");

    addExpectedVariable("Activity_shared-add-factors_completer", testUserName);
    addExpectedCephContent(addFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-factors.json");
    addExpectedCephContent(checkComplianceActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");

    // check compliance
    assertWaitingActivity(checkComplianceActivityDefinitionKey, "shared-check-complience");

    completeTask(checkComplianceActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-check-compliance_errors.json");

    addExpectedCephContent(checkComplianceActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-check-compliance_errors.json");
    addExpectedCephContent(addDecisionDenyActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-solution-deny-prepopulation.json");
    addExpectedVariable("solutionTypeId", "woconsider");
    addExpectedVariable("Activity_shared-check-complience_completer", testUserName);
    // add decision include
    assertWaitingActivity(addDecisionDenyActivityDefinitionKey, "shared-add-decision-deny");

    completeTask(addDecisionDenyActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-decision-deny.json");

    addExpectedVariable("Activity_shared-add-decision-deny_completer", testUserName);
    addExpectedCephContent(addDecisionDenyActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-decision-deny.json");
    addExpectedCephContent(addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-solution-deny-prepopulation.json");

    // add letter data
    assertWaitingActivity(addLetterDataActivityDefinitionKey, "shared-add-letter-data");

    completeTask(addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-letter-data.json");

    addExpectedVariable("Activity_1eujure_completer", testUserName);
    addExpectedCephContent(addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-letter-data.json");
    addExpectedCephContent(signAppDenyActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-deny_prepopulation.json");

    // sign app deny
    assertWaitingActivity(signAppDenyActivityDefinitionKey, "shared-sign-app-deny");

    completeTask(signAppDenyActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-deny.json");

    addExpectedVariable("Activity_shared-sign-app-deny_completer", testUserName);
    addExpectedCephContent(signAppDenyActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-deny.json");

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(currentProcessInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);

    addExpectedVariable("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
    addExpectedVariable("sys-var-process-completion-result",
        "Прийнято рішення про залишення без розгляду");

    assertSystemSignature("system_signature_ceph_key",
        "/json/create-app/dso/expanseDenySystemSignatureCephContent.json");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();

    mockServer.verify();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testValidationError() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";

    mockEdrResponse("/json/create-app/edr/searchSubjectsActiveResponse.json");

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/create-app/data-factory/last-laboratory-solution-add.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/applicationTypeResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/solutionTypeAddResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response(
            "/json/create-app/data-factory/findLaboratoryWithoutAccreditationResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-equal-laboratory-id-count")
        .queryParams(Map.of("laboratoryId", labId))
        .response("[]")
        .build());

    var ex = org.junit.jupiter.api.Assertions.assertThrows(ValidationException.class,
        () -> startProcessInstanceWithStartForm(labId));

    Assertions.assertThat(ex.getDetails().getErrors()).hasSize(1);
    Assertions.assertThat(ex.getDetails().getErrors())
        .contains(new ErrorDetailDto("Додайте кадровий склад до лабораторії", "laboratory", labId));

    mockServer.verify();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testNoAccreditationFlag() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";

    var addApplicationActivityDefinitionKey = "Activity_shared-add-application";

    mockEdrResponse("/json/create-app/edr/searchSubjectsActiveResponse.json");

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/create-app/data-factory/last-laboratory-solution-add.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/applicationTypeResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/solutionTypeAddResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response(
            "/json/create-app/data-factory/findLaboratoryWithoutAccreditationResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-equal-laboratory-id-count")
        .queryParams(Map.of("laboratoryId", labId))
        .response("[{\"cnt\":1}]")
        .build());

    startProcessInstanceWithStartForm(labId);

    expectedVariablesMap.put("initiator", testUserName);
    expectedVariablesMap.put("fullName", null);
    expectedVariablesMap.put("start_form_ceph_key", START_FORM_CEPH_KEY);
    addExpectedCephContent(addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/name-edrpou-prepopulation.json");

    assertWaitingActivity(addApplicationActivityDefinitionKey, "shared-add-application");

    mockServer.verify();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testAppAlreadyCreated() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";

    mockEdrResponse("/json/create-app/edr/searchSubjectsActiveResponse.json");

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/create-app/data-factory/last-laboratory-solution-deny.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/applicationTypeResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/solutionTypeAddResponse.json")
        .build());

    var ex = org.junit.jupiter.api.Assertions.assertThrows(ValidationException.class,
        () -> startProcessInstanceWithStartForm(labId));

    Assertions.assertThat(ex.getDetails().getErrors()).hasSize(1);
    Assertions.assertThat(ex.getDetails().getErrors())
        .contains(
            new ErrorDetailDto("Заява на первинне внесення не створена", "laboratory", labId));

    mockServer.verify();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testSubjectIsDisabledButNoErrors() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";

    var subjectStatusErrorActivityDefinitionKey = "Activity_shared-subject-status-error";
    var addApplicationActivityDefinitionKey = "Activity_shared-add-application";
    var addFactorsActivityDefinitionKey = "Activity_shared-add-factors";
    var checkComplianceActivityDefinitionKey = "Activity_shared-check-complience";

    mockEdrResponse("/json/create-app/edr/searchSubjectsDisabledResponse.json");

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app/data-factory/findLaboratoryResponse.json")
        .build());

    startProcessInstanceWithStartForm(labId);

    expectedVariablesMap.put("initiator", testUserName);
    expectedVariablesMap.put("start_form_ceph_key", START_FORM_CEPH_KEY);

    // subject status error
    assertWaitingActivity(subjectStatusErrorActivityDefinitionKey,
        "shared-subject-status-error");
    completeTask(subjectStatusErrorActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-subject-status-error.json");
    addExpectedCephContent(subjectStatusErrorActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-subject-status-error.json");
    expectedVariablesMap.put("Activity_shared-subject-status-error_completer", testUserName);
    expectedVariablesMap.put("edrSuspendedOrCancelled", "true");
    expectedVariablesMap.put("fullName", null);
    // add application
    assertWaitingActivity(addApplicationActivityDefinitionKey, "shared-add-application");

    completeTask(addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-application.json");

    expectedVariablesMap.put("Activity_shared-add-application_completer", testUserName);
    addExpectedCephContent(addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-application.json");
    addExpectedCephContent(addFactorsActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");
    // add bio phys labor and chem factors
    assertWaitingActivity(addFactorsActivityDefinitionKey, "shared-add-factors");

    completeTask(addFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-factors.json");

    expectedVariablesMap.put("Activity_shared-add-factors_completer", testUserName);
    addExpectedCephContent(addFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-factors.json");
    // check compliance
    assertWaitingActivity(checkComplianceActivityDefinitionKey, "shared-check-complience");

    var ex = org.junit.jupiter.api.Assertions.assertThrows(ValidationException.class,
        () -> completeTask(checkComplianceActivityDefinitionKey,
            "/json/create-app/form-data/Activity_shared-check-compliance_noErrors.json"));

    Assertions.assertThat(ex.getDetails().getErrors()).hasSize(1);
    Assertions.assertThat(ex.getDetails().getErrors()).contains(
        new ErrorDetailDto("Статус суб'єкта в ЄДР \"Скаcовано\" або \"Припинено\","
            + " оберіть відповідну причину відмови", "errorCheckFlag", "false"));

    mockServer.verify();
  }

  protected void startProcessInstanceWithStartForm(String labId) {
    saveStartFormDataToCeph(labId);
    startProcessInstance(PROCESS_DEFINITION_ID,
        Map.of(Constants.BPMS_START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY,
            "initiator", testUserName));
  }

  private void saveStartFormDataToCeph(String labId) {
    var data = new LinkedHashMap<String, Object>();
    data.put("laboratory", Map.of("laboratoryId", labId));
    data.put("edrpou", "77777777");
    data.put("subjectType", "LEGAL");
    data.put("subject", Map.of("subjectId", "activeSubject"));
    cephService.putFormData(START_FORM_CEPH_KEY, FormDataDto.builder().data(data).build());
  }
}
