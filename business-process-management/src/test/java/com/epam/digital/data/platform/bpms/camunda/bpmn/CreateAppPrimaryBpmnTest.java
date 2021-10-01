package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.historyService;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.bpms.camunda.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpms.camunda.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorDetailDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CreateAppPrimaryBpmnTest extends BaseBpmnTest {

  private static final String PROCESS_DEFINITION_KEY = "create-app-primary";

  @Test
  @Deployment(resources = {"bpmn/create-app-primary.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testAdditionHappyPass_accreditationFlagIsTrue() {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
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
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/applicationTypeResponse.json")
        .build());
    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-app/dso/primaryIncludeSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("registration")
        .requestBody("/json/create-app/data-factory/createApplicationIncludeRequest.json")
        .response("{}")
        .build());

    var data = deserializeFormData("/json/create-app/form-data/start_event.json");
    startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, data);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-application")
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-edrpou-prepopulation.json"))
        .expectedVariables(
            Map.of("initiator", testUserName, "start_form_ceph_key", START_FORM_CEPH_KEY,
                "fullName", "testuser testuser testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-application")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app/form-data/Activity_shared-add-application.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-factors")
        .formKey("shared-add-factors")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-add-application_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-factors")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app/form-data/Activity_shared-add-factors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-check-complience")
        .formKey("shared-check-complience")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-add-application_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-check-complience")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-check-compliance_noErrors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-decision-include")
        .formKey("shared-add-decision-include")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-solution-include-prepopulation.json"))
        .expectedVariables(
            Map.of("Activity_shared-check-complience_completer", testUserName, "solutionTypeId",
                "ADD=SUCCESS"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-decision-include")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-add-decision-include.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-letter-data")
        .formKey("shared-add-letter-data")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-solution-include-prepopulation.json"))
        .expectedVariables(
            Map.of("Activity_shared-add-decision-include_completer", testUserName, "solutionTypeId",
                "ADD=SUCCESS"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-letter-data")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-add-letter-data.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-sign-app-include")
        .formKey("shared-sign-app-include")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/Activity_shared-sign-app-include_prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-add-letter-data_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-sign-app-include")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-sign-app-include.json")
        .build());

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(currentProcessInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";

    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);
    addExpectedVariable("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
    addExpectedVariable("Activity_shared-sign-app-include_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Прийнято рішення про внесення лабораторії до переліку");

    assertSystemSignature("system_signature_ceph_key",
        "/json/create-app/dso/primaryIncludeSystemSignatureCephContent.json");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/create-app-primary.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testDenyingHappyPass_accreditationFlagIsTrue() {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
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
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/applicationTypeResponse.json")
        .build());
    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-app/dso/primaryDenySystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("registration")
        .requestBody("/json/create-app/data-factory/createApplicationPrimaryDenyRequest.json")
        .response("{}")
        .build());

    var data = deserializeFormData("/json/create-app/form-data/start_event.json");
    startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, data);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-subject-status-error")
        .formKey("shared-subject-status-error")
        .assignee(testUserName)
        .expectedVariables(
            Map.of("initiator", testUserName, "start_form_ceph_key", START_FORM_CEPH_KEY))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-subject-status-error")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app/form-data/Activity_shared-subject-status-error.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-application")
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-edrpou-prepopulation.json"))
        .expectedVariables(Map.of("edrSuspendedOrCancelled", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-application")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app/form-data/Activity_shared-add-application.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-factors")
        .formKey("shared-add-factors")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-add-application_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-factors")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app/form-data/Activity_shared-add-factors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-check-complience")
        .formKey("shared-check-complience")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-add-application_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-check-complience")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-check-compliance_errors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-decision-deny")
        .formKey("shared-add-decision-deny")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-solution-deny-prepopulation.json"))
        .expectedVariables(
            Map.of("Activity_shared-check-complience_completer", testUserName, "solutionTypeId",
                "woconsider"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-decision-deny")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-add-decision-deny.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_1eujure")
        .formKey("shared-add-letter-data")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-solution-deny-prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-add-decision-deny_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_1eujure")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-add-letter-data.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-sign-app-deny")
        .formKey("shared-sign-app-deny")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/Activity_shared-sign-app-deny_prepopulation.json"))
        .expectedVariables(Map.of("Activity_1eujure_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-sign-app-deny")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-sign-app-deny.json")
        .build());

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(currentProcessInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(1);

    var systemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";

    addExpectedVariable("system_signature_ceph_key", systemSignatureCephKey);
    addExpectedVariable("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
    addExpectedVariable("Activity_shared-sign-app-deny_completer", testUserName);
    addExpectedVariable("fullName", null);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Прийнято рішення про залишення без розгляду");

    assertSystemSignature("system_signature_ceph_key",
        "/json/create-app/dso/primaryDenySystemSignatureCephContent.json");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-primary.bpmn")
  public void testValidationError() {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
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

    var data = deserializeFormData("/json/create-app/form-data/start_event.json");

    var ex = assertThrows(ValidationException.class,
        () -> startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, data));

    Assertions.assertThat(ex.getDetails().getErrors()).hasSize(1);
    Assertions.assertThat(ex.getDetails().getErrors())
        .contains(new ErrorDetailDto("Додайте кадровий склад до лабораторії", "laboratory", labId));
    mockServer.verify();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-primary.bpmn")
  public void testNoAccreditationFlag() {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
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

    var data = deserializeFormData("/json/create-app/form-data/start_event.json");
    startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, data);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-application")
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-edrpou-prepopulation.json"))
        .expectedVariables(
            Map.of("initiator", testUserName, "start_form_ceph_key", START_FORM_CEPH_KEY,
                "fullName", "testuser testuser testuser"))
        .build());

    mockServer.verify();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-primary.bpmn")
  public void testAppAlreadyCreated() {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
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

    var data = deserializeFormData("/json/create-app/form-data/start_event.json");

    var ex = assertThrows(ValidationException.class,
        () -> startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, data));

    Assertions.assertThat(ex.getDetails().getErrors()).hasSize(1);
    Assertions.assertThat(ex.getDetails().getErrors())
        .contains(
            new ErrorDetailDto("Заява на первинне внесення вже створена", "laboratory", labId));
    mockServer.verify();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-primary.bpmn")
  public void testSubjectIsDisabledButNoErrors() {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    mockEdrResponse("/json/create-app/edr/searchSubjectsDisabledResponse.json");
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app/data-factory/findLaboratoryResponse.json")
        .build());

    var data = deserializeFormData("/json/create-app/form-data/start_event.json");
    startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, data);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-subject-status-error")
        .formKey("shared-subject-status-error")
        .assignee(testUserName)
        .expectedVariables(
            Map.of("initiator", testUserName, "start_form_ceph_key", START_FORM_CEPH_KEY))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-subject-status-error")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app/form-data/Activity_shared-subject-status-error.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-application")
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-edrpou-prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-subject-status-error_completer", testUserName,
            "edrSuspendedOrCancelled", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-application")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app/form-data/Activity_shared-add-application.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-factors")
        .formKey("shared-add-factors")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-add-application_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-add-factors")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app/form-data/Activity_shared-add-factors.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("Activity_shared-check-complience")
        .formKey("shared-check-complience")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app/form-data/name-registrationNo-prepopulation.json"))
        .expectedVariables(Map.of("Activity_shared-add-factors_completer", testUserName))
        .build());

    var ex = assertThrows(ValidationException.class,
        () -> completeTask("Activity_shared-check-complience",
            "/json/create-app/form-data/Activity_shared-check-compliance_noErrors.json"));

    Assertions.assertThat(ex.getDetails().getErrors()).hasSize(1);
    Assertions.assertThat(ex.getDetails().getErrors()).contains(
        new ErrorDetailDto("Статус суб'єкта в ЄДР \"Скаcовано\" або \"Припинено\","
            + " оберіть відповідну причину відмови", "errorCheckFlag", "false"));
    mockServer.verify();
  }
}
