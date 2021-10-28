package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.historyService;

import com.epam.digital.data.platform.bpms.camunda.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpms.camunda.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorDetailDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CreateAppExpanseBpmnTest extends BaseBpmnTest {

  private static final String PROCESS_DEFINITION_KEY = "create-app-expanse";
  private static final String START_FORM_CEPH_KEY = "startFormCephKey";

  @Test
  @Deployment(resources = {"bpmn/create-app-expanse.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testAdditionHappyPass_accreditationFlagIsTrue() {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    var addApplicationActivityDefinitionKey = "Activity_shared-add-application";
    var addFactorsActivityDefinitionKey = "Activity_shared-add-factors";
    var checkComplianceActivityDefinitionKey = "Activity_shared-check-complience";
    var addDecisionIncludeActivityDefinitionKey = "Activity_shared-add-decision-include";
    var addLetterDataActivityDefinitionKey = "Activity_shared-add-letter-data";
    var signAppIncludeActivityDefinitionKey = "Activity_shared-sign-app-include";

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    mockEdrResponse("/json/create-app-expanse/edr/searchSubjectsActiveResponse.json");
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/create-app-expanse/data-factory/last-laboratory-solution-add.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app-expanse/data-factory/applicationTypeResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app-expanse/data-factory/solutionTypeAddResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app-expanse/data-factory/findLaboratoryResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app-expanse/data-factory/solutionTypeAddResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "EXPANSE"))
        .response("/json/create-app-expanse/data-factory/applicationTypeExpanseResponse.json")
        .build());
    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-app-expanse/dso/expanseIncludeSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("registration")
        .requestBody("/json/create-app-expanse/data-factory/createApplicationExpanseRequest.json")
        .response("{}")
        .build());

    var data = deserializeFormData("/json/create-app-expanse/form-data/start_event.json");
    startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, data);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addApplicationActivityDefinitionKey)
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/addApplicationFormActivityPrePopulation.json"))
        .expectedVariables(Map.of("initiator", testUserName,
            "fullName", "testuser testuser testuser",
            StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addApplicationActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app-expanse/form-data/Activity_shared-add-application.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addFactorsActivityDefinitionKey)
        .formKey("shared-add-factors")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of(addApplicationActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addFactorsActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-add-factors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(checkComplianceActivityDefinitionKey)
        .formKey("shared-check-complience")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of(addFactorsActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(checkComplianceActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-check-compliance_noErrors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addDecisionIncludeActivityDefinitionKey)
        .formKey("shared-add-decision-include")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-solution-include-prepopulation.json"))
        .expectedVariables(Map.of(checkComplianceActivityDefinitionKey + "_completer", testUserName,
            "solutionTypeId", "ADD=SUCCESS"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addDecisionIncludeActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-add-decision-include.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addLetterDataActivityDefinitionKey)
        .formKey("shared-add-letter-data")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-solution-include-prepopulation.json"))
        .expectedVariables(
            Map.of(addDecisionIncludeActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addLetterDataActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-add-letter-data.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(signAppIncludeActivityDefinitionKey)
        .formKey("shared-sign-app-include")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/Activity_shared-sign-app-include_prepopulation.json"))
        .expectedVariables(Map.of(addLetterDataActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(signAppIncludeActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-sign-app-include.json")
        .build());

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(currentProcessInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";

    addExpectedVariable("Activity_shared-sign-app-include_completer", testUserName);
    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);
    addExpectedVariable("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Прийнято рішення про розширення факторів");

    assertSystemSignature("system_signature_ceph_key",
        "/json/create-app-expanse/dso/expanseIncludeSystemSignatureCephContent.json");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/create-app-expanse.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testDenyingHappyPass_accreditationFlagIsTrue() {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    var subjectStatusErrorActivityDefinitionKey = "Activity_shared-subject-status-error";
    var addApplicationActivityDefinitionKey = "Activity_shared-add-application";
    var addFactorsActivityDefinitionKey = "Activity_shared-add-factors";
    var checkComplianceActivityDefinitionKey = "Activity_shared-check-complience";
    var addDecisionDenyActivityDefinitionKey = "Activity_shared-add-decision-deny";
    var addLetterDataActivityDefinitionKey = "Activity_1eujure";
    var signAppDenyActivityDefinitionKey = "Activity_shared-sign-app-deny";

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    mockEdrResponse("/json/create-app-expanse/edr/searchSubjectsDisabledResponse.json");
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app-expanse/data-factory/findLaboratoryResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "WO_CONSIDER"))
        .response("/json/create-app-expanse/data-factory/solutionTypeWoConsiderResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "EXPANSE"))
        .response("/json/create-app-expanse/data-factory/applicationTypeExpanseResponse.json")
        .build());
    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-app-expanse/dso/expanseDenySystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("registration")
        .requestBody("/json/create-app-expanse/data-factory/createApplicationExpanseDenyRequest.json")
        .response("{}")
        .build());

    var data = deserializeFormData("/json/create-app-expanse/form-data/start_event.json");
    startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, data);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(subjectStatusErrorActivityDefinitionKey)
        .formKey("shared-subject-status-error")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName,
            StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(subjectStatusErrorActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-subject-status-error.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addApplicationActivityDefinitionKey)
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/addApplicationFormActivityPrePopulation.json"))
        .expectedVariables(
            Map.of(subjectStatusErrorActivityDefinitionKey + "_completer", testUserName,
                "edrSuspendedOrCancelled", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addApplicationActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app-expanse/form-data/Activity_shared-add-application.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addFactorsActivityDefinitionKey)
        .formKey("shared-add-factors")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of(addApplicationActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addFactorsActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-add-factors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(checkComplianceActivityDefinitionKey)
        .formKey("shared-check-complience")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of(addFactorsActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(checkComplianceActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-check-compliance_errors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addDecisionDenyActivityDefinitionKey)
        .formKey("shared-add-decision-deny")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-solution-deny-prepopulation.json"))
        .expectedVariables(Map.of(checkComplianceActivityDefinitionKey + "_completer", testUserName,
            "solutionTypeId", "woconsider"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addDecisionDenyActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-add-decision-deny.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addLetterDataActivityDefinitionKey)
        .formKey("shared-add-letter-data")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-solution-deny-prepopulation.json"))
        .expectedVariables(
            Map.of(addDecisionDenyActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addLetterDataActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app-expanse/form-data/Activity_shared-add-letter-data.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(signAppDenyActivityDefinitionKey)
        .formKey("shared-sign-app-deny")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/Activity_shared-sign-app-deny_prepopulation.json"))
        .expectedVariables(Map.of(addLetterDataActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(signAppDenyActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app-expanse/form-data/Activity_shared-sign-app-deny.json")
        .build());

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(currentProcessInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";

    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);
    addExpectedVariable("Activity_shared-sign-app-deny_completer", testUserName);
    addExpectedVariable("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Прийнято рішення про залишення без розгляду");

    assertSystemSignature("system_signature_ceph_key",
        "/json/create-app-expanse/dso/expanseDenySystemSignatureCephContent.json");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testValidationError() {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    mockEdrResponse("/json/create-app-expanse/edr/searchSubjectsActiveResponse.json");
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/create-app-expanse/data-factory/last-laboratory-solution-add.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app-expanse/data-factory/applicationTypeResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app-expanse/data-factory/solutionTypeAddResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response(
            "/json/create-app-expanse/data-factory/findLaboratoryWithoutAccreditationResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-equal-laboratory-id-count")
        .queryParams(Map.of("laboratoryId", labId))
        .response("[]")
        .build());

    var data = deserializeFormData("/json/create-app-expanse/form-data/start_event.json");

    var ex = org.junit.jupiter.api.Assertions.assertThrows(ValidationException.class,
        () -> startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, data));

    Assertions.assertThat(ex.getDetails().getErrors()).hasSize(1);
    Assertions.assertThat(ex.getDetails().getErrors())
        .contains(new ErrorDetailDto("Додайте кадровий склад до лабораторії", "laboratory", labId));
    mockServer.verify();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testNoAccreditationFlag() {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    mockEdrResponse("/json/create-app-expanse/edr/searchSubjectsActiveResponse.json");
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/create-app-expanse/data-factory/last-laboratory-solution-add.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app-expanse/data-factory/applicationTypeResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app-expanse/data-factory/solutionTypeAddResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response(
            "/json/create-app-expanse/data-factory/findLaboratoryWithoutAccreditationResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-equal-laboratory-id-count")
        .queryParams(Map.of("laboratoryId", labId))
        .response("[{\"cnt\":1}]")
        .build());

    var data = deserializeFormData("/json/create-app-expanse/form-data/start_event.json");
    startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, data);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-application")
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-edrpou-prepopulation.json"))
        .expectedVariables(Map.of("initiator", testUserName,
            "fullName", "testuser testuser testuser",
            StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY))
        .build());

    mockServer.verify();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testAppAlreadyCreated() {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    mockEdrResponse("/json/create-app-expanse/edr/searchSubjectsActiveResponse.json");
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Map.of("laboratoryId", labId))
        .response("/json/create-app-expanse/data-factory/last-laboratory-solution-deny.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app-expanse/data-factory/applicationTypeResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app-expanse/data-factory/solutionTypeAddResponse.json")
        .build());

    var data = deserializeFormData("/json/create-app-expanse/form-data/start_event.json");

    var ex = org.junit.jupiter.api.Assertions.assertThrows(ValidationException.class,
        () -> startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, data));

    Assertions.assertThat(ex.getDetails().getErrors()).hasSize(1);
    Assertions.assertThat(ex.getDetails().getErrors())
        .contains(
            new ErrorDetailDto("Заява на первинне внесення не створена", "laboratory", labId));

    mockServer.verify();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testSubjectIsDisabledButNoErrors() {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    var subjectStatusErrorActivityDefinitionKey = "Activity_shared-subject-status-error";
    var addApplicationActivityDefinitionKey = "Activity_shared-add-application";
    var addFactorsActivityDefinitionKey = "Activity_shared-add-factors";
    var checkComplianceActivityDefinitionKey = "Activity_shared-check-complience";

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    mockEdrResponse("/json/create-app-expanse/edr/searchSubjectsDisabledResponse.json");
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app-expanse/data-factory/findLaboratoryResponse.json")
        .build());

    var data = deserializeFormData("/json/create-app-expanse/form-data/start_event.json");
    startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, data);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(subjectStatusErrorActivityDefinitionKey)
        .formKey("shared-subject-status-error")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName,
            StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(subjectStatusErrorActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-subject-status-error.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addApplicationActivityDefinitionKey)
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/addApplicationFormActivityPrePopulation.json"))
        .expectedVariables(
            Map.of(subjectStatusErrorActivityDefinitionKey + "_completer", testUserName,
                "edrSuspendedOrCancelled", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addApplicationActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app-expanse/form-data/Activity_shared-add-application.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addFactorsActivityDefinitionKey)
        .formKey("shared-add-factors")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of(addApplicationActivityDefinitionKey + "_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(addFactorsActivityDefinitionKey)
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-expanse/form-data/Activity_shared-add-factors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId(checkComplianceActivityDefinitionKey)
        .formKey("shared-check-complience")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-expanse/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of(addFactorsActivityDefinitionKey + "_completer", testUserName))
        .build());
    var ex = org.junit.jupiter.api.Assertions.assertThrows(ValidationException.class,
        () -> completeTask(checkComplianceActivityDefinitionKey,
            "/json/create-app-expanse/form-data/Activity_shared-check-compliance_noErrors.json"));

    Assertions.assertThat(ex.getDetails().getErrors()).hasSize(1);
    Assertions.assertThat(ex.getDetails().getErrors()).contains(
        new ErrorDetailDto("Статус суб'єкта в ЄДР \"Скаcовано\" або \"Припинено\","
            + " оберіть відповідну причину відмови", "errorCheckFlag", "false"));
    mockServer.verify();
  }
}
