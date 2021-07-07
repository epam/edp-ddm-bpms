package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.junit.Assert.assertThrows;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorDetailDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import java.io.IOException;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

public class CreateAppExpanseBpmnIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_ID = "create-app-expanse";

  @Value("${camunda.system-variables.const_dataFactoryBaseUrl}")
  private String dataFactoryBaseUrl;

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testAdditionHappyPass_accreditationFlagIsTrue() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    var searchLabFormActivityDefinitionKey = "Activity_shared-search-lab";
    var addApplicationActivityDefinitionKey = "Activity_shared-add-application";
    var addBioPhysLaborFactorsActivityDefinitionKey = "Activity_shared-add-bio-phys-labor-factors";
    var addChemFactorsActivityDefinitionKey = "Activity_shared-add-chem-factors";
    var checkComplianceActivityDefinitionKey = "Activity_shared-check-complience";
    var addDecisionIncludeActivityDefinitionKey = "Activity_shared-add-decision-include";
    var addLetterDataActivityDefinitionKey = "Activity_shared-add-letter-data";
    var signAppIncludeActivityDefinitionKey = "Activity_shared-sign-app-include";

    stubDataFactoryGet(StubData.builder()
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app/data-factory/findLaboratoryResponse.json")
        .build());

    stubDataFactoryGet(StubData.builder()
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/solutionTypeAddResponse.json")
        .build());

    stubDataFactoryGet(StubData.builder()
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "EXPANSE"))
        .response("/json/create-app/data-factory/applicationTypeExpanseResponse.json")
        .build());

    stubDigitalSignature(StubData.builder()
        .requestBody("/json/create-app/dso/expanseIncludeSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryCreate(StubData.builder()
        .resource("registration")
        .requestBody("/json/create-app/data-factory/createApplicationExpanseRequest.json")
        .response("{}")
        .build());

    var processInstance = runtimeService().startProcessInstanceByKey(PROCESS_DEFINITION_ID);
    assertThat(processInstance).isStarted();

    var processInstanceId = processInstance.getId();
    String initiator = null;

    expectedVariablesMap.put("initiator", initiator);
    expectedVariablesMap.put("const_dataFactoryBaseUrl", dataFactoryBaseUrl);

    // search lab
    assertWaitingActivity(processInstance, searchLabFormActivityDefinitionKey, "shared-search-lab");

    completeTask(searchLabFormActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-search-lab.json");

    addCompleterUsernameVariable(searchLabFormActivityDefinitionKey, null);
    addExpectedCephContent(processInstanceId, addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/name-edrpou-prepopulation.json");
    addExpectedCephContent(processInstanceId, searchLabFormActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-search-lab.json");

    expectedVariablesMap.put("laboratoryId", labId);

    // add application
    assertWaitingActivity(processInstance, addApplicationActivityDefinitionKey,
        "shared-add-application");

    completeTask(addApplicationActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-application.json");

    addCompleterUsernameVariable(addApplicationActivityDefinitionKey, null);
    addExpectedCephContent(processInstanceId, addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-application.json");
    addExpectedCephContent(processInstanceId, addBioPhysLaborFactorsActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");
    addExpectedCephContent(processInstanceId, addChemFactorsActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");
    // add bio phys labor and chem factors
    assertWaitingActivity(processInstance, addBioPhysLaborFactorsActivityDefinitionKey,
        "shared-add-bio-phys-labor-factors");
    assertWaitingActivity(processInstance, addChemFactorsActivityDefinitionKey,
        "shared-add-chem-factors");

    completeTask(addBioPhysLaborFactorsActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-bio-phys-labor-factors.json");
    completeTask(addChemFactorsActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-chem-factors.json");

    addCompleterUsernameVariable(addBioPhysLaborFactorsActivityDefinitionKey, null);
    addCompleterUsernameVariable(addChemFactorsActivityDefinitionKey, null);
    addExpectedCephContent(processInstanceId, addBioPhysLaborFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-bio-phys-labor-factors.json");
    addExpectedCephContent(processInstanceId, addChemFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-chem-factors.json");
    addExpectedCephContent(processInstanceId, checkComplianceActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");

    // check compliance
    assertWaitingActivity(processInstance, checkComplianceActivityDefinitionKey,
        "shared-check-complience");

    completeTask(checkComplianceActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-check-compliance_noErrors.json");

    addCompleterUsernameVariable(checkComplianceActivityDefinitionKey, null);
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

    addCompleterUsernameVariable(addDecisionIncludeActivityDefinitionKey, null);
    addExpectedCephContent(processInstanceId, addDecisionIncludeActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-decision-include.json");
    addExpectedCephContent(processInstanceId, addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-solution-include-prepopulation.json");

    // add letter data
    assertWaitingActivity(processInstance, addLetterDataActivityDefinitionKey,
        "shared-add-letter-data");

    completeTask(addLetterDataActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-letter-data.json");

    addCompleterUsernameVariable(addLetterDataActivityDefinitionKey, null);
    addExpectedCephContent(processInstanceId, addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-letter-data.json");
    addExpectedCephContent(processInstanceId, signAppIncludeActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-include_prepopulation.json");

    // sign app include
    assertWaitingActivity(processInstance, signAppIncludeActivityDefinitionKey,
        "shared-sign-app-include");

    completeTask(signAppIncludeActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-sign-app-include.json");

    addCompleterUsernameVariable(signAppIncludeActivityDefinitionKey, null);
    addExpectedCephContent(processInstanceId, signAppIncludeActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-include.json");

    var systemSignatureCephKeyRefVarName = "system_signature_ceph_key";
    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        systemSignatureCephKeyRefVarName;

    expectedVariablesMap.put("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
    expectedVariablesMap.put("sys-var-process-completion-result",
        "Прийнято рішення про розширення факторів");

    String signature = cephService.getContent(cephBucketName, systemSignatureCephKey);
    Map<String, Object> signatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(signature);
    Map<String, Object> expectedSignatureMap = objectMapper.readerForMapOf(Object.class).readValue(
        TestUtils.getContent("/json/create-app/dso/expanseIncludeSystemSignatureCephContent.json"));
    Assertions.assertThat(signatureMap).isEqualTo(expectedSignatureMap);

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testDenyingHappyPass_accreditationFlagIsTrue() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    var searchLabFormActivityDefinitionKey = "Activity_shared-search-lab";
    var addApplicationActivityDefinitionKey = "Activity_shared-add-application";
    var addBioPhysLaborFactorsActivityDefinitionKey = "Activity_shared-add-bio-phys-labor-factors";
    var addChemFactorsActivityDefinitionKey = "Activity_shared-add-chem-factors";
    var checkComplianceActivityDefinitionKey = "Activity_shared-check-complience";
    var addDecisionDenyActivityDefinitionKey = "Activity_shared-add-decision-deny";
    var addLetterDataActivityDefinitionKey = "Activity_1eujure";
    var signAppDenyActivityDefinitionKey = "Activity_shared-sign-app-deny";

    stubDataFactoryGet(StubData.builder()
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app/data-factory/findLaboratoryResponse.json")
        .build());

    stubDataFactoryGet(StubData.builder()
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "WO_CONSIDER"))
        .response("/json/create-app/data-factory/solutionTypeWoConsiderResponse.json")
        .build());

    stubDataFactoryGet(StubData.builder()
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "EXPANSE"))
        .response("/json/create-app/data-factory/applicationTypeExpanseResponse.json")
        .build());

    stubDigitalSignature(StubData.builder()
        .requestBody("/json/create-app/dso/expanseDenySystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryCreate(StubData.builder()
        .resource("registration")
        .requestBody("/json/create-app/data-factory/createApplicationExpanseDenyRequest.json")
        .response("{}")
        .build());

    var processInstance = runtimeService().startProcessInstanceByKey(PROCESS_DEFINITION_ID);
    assertThat(processInstance).isStarted();

    var processInstanceId = processInstance.getId();
    String initiator = null;

    expectedVariablesMap.put("initiator", initiator);
    expectedVariablesMap.put("const_dataFactoryBaseUrl", dataFactoryBaseUrl);

    // search lab
    assertWaitingActivity(processInstance, searchLabFormActivityDefinitionKey, "shared-search-lab");

    completeTask(searchLabFormActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-search-lab.json");

    addCompleterUsernameVariable(searchLabFormActivityDefinitionKey, null);
    addExpectedCephContent(processInstanceId, addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/name-edrpou-prepopulation.json");
    addExpectedCephContent(processInstanceId, searchLabFormActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-search-lab.json");

    expectedVariablesMap.put("laboratoryId", labId);

    // add application
    assertWaitingActivity(processInstance, addApplicationActivityDefinitionKey,
        "shared-add-application");

    completeTask(addApplicationActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-application.json");

    addCompleterUsernameVariable(addApplicationActivityDefinitionKey, null);
    addExpectedCephContent(processInstanceId, addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-application.json");
    addExpectedCephContent(processInstanceId, addBioPhysLaborFactorsActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");
    addExpectedCephContent(processInstanceId, addChemFactorsActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");
    // add bio phys labor and chem factors
    assertWaitingActivity(processInstance, addBioPhysLaborFactorsActivityDefinitionKey,
        "shared-add-bio-phys-labor-factors");
    assertWaitingActivity(processInstance, addChemFactorsActivityDefinitionKey,
        "shared-add-chem-factors");

    completeTask(addBioPhysLaborFactorsActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-bio-phys-labor-factors.json");
    completeTask(addChemFactorsActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-chem-factors.json");

    addCompleterUsernameVariable(addBioPhysLaborFactorsActivityDefinitionKey, null);
    addCompleterUsernameVariable(addChemFactorsActivityDefinitionKey, null);
    addExpectedCephContent(processInstanceId, addBioPhysLaborFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-bio-phys-labor-factors.json");
    addExpectedCephContent(processInstanceId, addChemFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-chem-factors.json");
    addExpectedCephContent(processInstanceId, checkComplianceActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");

    // check compliance
    assertWaitingActivity(processInstance, checkComplianceActivityDefinitionKey,
        "shared-check-complience");

    completeTask(checkComplianceActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-check-compliance_errors.json");

    addCompleterUsernameVariable(checkComplianceActivityDefinitionKey, null);
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

    addCompleterUsernameVariable(addDecisionDenyActivityDefinitionKey, null);
    addExpectedCephContent(processInstanceId, addDecisionDenyActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-decision-deny.json");
    addExpectedCephContent(processInstanceId, addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-solution-deny-prepopulation.json");

    // add letter data
    assertWaitingActivity(processInstance, addLetterDataActivityDefinitionKey,
        "shared-add-letter-data");

    completeTask(addLetterDataActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-add-letter-data.json");

    addCompleterUsernameVariable(addLetterDataActivityDefinitionKey, null);
    addExpectedCephContent(processInstanceId, addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-letter-data.json");
    addExpectedCephContent(processInstanceId, signAppDenyActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-deny_prepopulation.json");

    // sign app deny
    assertWaitingActivity(processInstance, signAppDenyActivityDefinitionKey,
        "shared-sign-app-deny");

    completeTask(signAppDenyActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-sign-app-deny.json");

    addCompleterUsernameVariable(signAppDenyActivityDefinitionKey, null);
    addExpectedCephContent(processInstanceId, signAppDenyActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-deny.json");

    var systemSignatureCephKeyRefVarName = "system_signature_ceph_key";
    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        systemSignatureCephKeyRefVarName;

    expectedVariablesMap.put("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
    expectedVariablesMap.put("sys-var-process-completion-result",
        "Прийнято рішення про залишення без розгляду");

    String signature = cephService.getContent(cephBucketName, systemSignatureCephKey);
    Map<String, Object> signatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(signature);
    Map<String, Object> expectedSignatureMap = objectMapper.readerForMapOf(Object.class).readValue(
        TestUtils.getContent("/json/create-app/dso/expanseDenySystemSignatureCephContent.json"));
    Assertions.assertThat(signatureMap).isEqualTo(expectedSignatureMap);

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testValidationError() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";

    var searchLabFormActivityDefinitionKey = "Activity_shared-search-lab";

    stubDataFactoryGet(StubData.builder()
        .resource("laboratory")
        .resourceId(labId)
        .response(
            "/json/create-app/data-factory/findLaboratoryWithoutAccreditationResponse.json")
        .build());

    stubDataFactoryGet(StubData.builder()
        .resource("staff-equal-laboratory-id-count")
        .queryParams(Map.of("laboratoryId", labId))
        .response("[]")
        .build());

    var processInstance = runtimeService().startProcessInstanceByKey(PROCESS_DEFINITION_ID);
    assertThat(processInstance).isStarted();

    var processInstanceId = processInstance.getId();
    String initiator = null;

    expectedVariablesMap.put("initiator", initiator);
    expectedVariablesMap.put("const_dataFactoryBaseUrl", dataFactoryBaseUrl);
    // search lab
    assertWaitingActivity(processInstance, searchLabFormActivityDefinitionKey, "shared-search-lab");

    var ex = assertThrows(ValidationException.class,
        () -> completeTask(searchLabFormActivityDefinitionKey, processInstanceId,
            "/json/create-app/form-data/Activity_shared-search-lab_without_accreditation.json"));
    assertWaitingActivity(processInstance, searchLabFormActivityDefinitionKey, "shared-search-lab");
    Assertions.assertThat(ex.getDetails().getErrors()).hasSize(1);
    Assertions.assertThat(ex.getDetails().getErrors())
        .contains(new ErrorDetailDto("Немає ознаки акредитованості", "id", labId));
  }

  @Test
  @Deployment(resources = "bpmn/create-app-expanse.bpmn")
  public void testNoAccreditationFlag() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";

    var searchLabFormActivityDefinitionKey = "Activity_shared-search-lab";
    var addApplicationActivityDefinitionKey = "Activity_shared-add-application";

    stubDataFactoryGet(StubData.builder()
        .resource("laboratory")
        .resourceId(labId)
        .response(
            "/json/create-app/data-factory/findLaboratoryWithoutAccreditationResponse.json")
        .build());

    stubDataFactoryGet(StubData.builder()
        .resource("staff-equal-laboratory-id-count")
        .queryParams(Map.of("laboratoryId", labId))
        .response("[{\"cnt\":1}]")
        .build());

    var processInstance = runtimeService().startProcessInstanceByKey(PROCESS_DEFINITION_ID);
    assertThat(processInstance).isStarted();

    var processInstanceId = processInstance.getId();
    String initiator = null;

    expectedVariablesMap.put("initiator", initiator);
    expectedVariablesMap.put("const_dataFactoryBaseUrl", dataFactoryBaseUrl);
    // search lab
    assertWaitingActivity(processInstance, searchLabFormActivityDefinitionKey, "shared-search-lab");

    completeTask(searchLabFormActivityDefinitionKey, processInstanceId,
        "/json/create-app/form-data/Activity_shared-search-lab_without_accreditation.json");

    addCompleterUsernameVariable(searchLabFormActivityDefinitionKey, null);
    expectedVariablesMap.put("laboratoryId", labId);
    addExpectedCephContent(processInstanceId, searchLabFormActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-search-lab_without_accreditation.json");
    addExpectedCephContent(processInstanceId, addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/name-edrpou-prepopulation.json");

    assertWaitingActivity(processInstance, addApplicationActivityDefinitionKey,
        "shared-add-application");
  }
}
