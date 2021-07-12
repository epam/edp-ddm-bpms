package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
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
import org.springframework.http.HttpMethod;

public class CreateAppPrimaryBpmnTest extends BaseBpmnTest {

  private static final String PROCESS_DEFINITION_ID = "create-app-primary";

  @Test
  @Deployment(resources = "bpmn/create-app-primary.bpmn")
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

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app/data-factory/findLaboratoryResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/solutionTypeAddResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/applicationTypeResponse.json")
        .build());

    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody("/json/create-app/dso/primaryIncludeSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("registration")
        .requestBody("/json/create-app/data-factory/createApplicationIncludeRequest.json")
        .response("{}")
        .build());

    startProcessInstance(PROCESS_DEFINITION_ID);

    addExpectedVariable("initiator", null);

    // search lab
    assertWaitingActivity(searchLabFormActivityDefinitionKey, "shared-search-lab");

    completeTask(searchLabFormActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-search-lab.json");

    addExpectedCephContent(addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/name-edrpou-prepopulation.json");
    addExpectedCephContent(searchLabFormActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-search-lab.json");

    addExpectedVariable("laboratoryId", labId);

    // add application
    assertWaitingActivity(addApplicationActivityDefinitionKey,
        "shared-add-application");

    completeTask(addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-application.json");

    addExpectedCephContent(addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-application.json");
    addExpectedCephContent(addBioPhysLaborFactorsActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");
    addExpectedCephContent(addChemFactorsActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");
    // add bio phys labor and chem factors
    assertWaitingActivity(addBioPhysLaborFactorsActivityDefinitionKey,
        "shared-add-bio-phys-labor-factors");
    assertWaitingActivity(addChemFactorsActivityDefinitionKey,
        "shared-add-chem-factors");

    completeTask(addBioPhysLaborFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-bio-phys-labor-factors.json");
    completeTask(addChemFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-chem-factors.json");

    addExpectedCephContent(addBioPhysLaborFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-bio-phys-labor-factors.json");
    addExpectedCephContent(addChemFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-chem-factors.json");
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
    // add decision include
    assertWaitingActivity(addDecisionIncludeActivityDefinitionKey,
        "shared-add-decision-include");

    completeTask(addDecisionIncludeActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-decision-include.json");

    addExpectedCephContent(addDecisionIncludeActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-decision-include.json");
    addExpectedCephContent(addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-solution-include-prepopulation.json");

    // add letter data
    assertWaitingActivity(addLetterDataActivityDefinitionKey, "shared-add-letter-data");

    completeTask(addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-letter-data.json");

    addExpectedCephContent(addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-letter-data.json");
    addExpectedCephContent(signAppIncludeActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-include_prepopulation.json");

    // sign app include
    assertWaitingActivity(signAppIncludeActivityDefinitionKey, "shared-sign-app-include");

    completeTask(signAppIncludeActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-include.json");

    addExpectedCephContent(signAppIncludeActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-include.json");

    var systemSignatureCephKeyRefVarName = "system_signature_ceph_key";
    var systemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        systemSignatureCephKeyRefVarName;

    addExpectedVariable("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
    addExpectedVariable("sys-var-process-completion-result",
        "Прийнято рішення про внесення лабораторії до переліку");

    var signature = cephService.getContent(cephBucketName, systemSignatureCephKey).get();
    var signatureMap = objectMapper.readerForMapOf(Object.class).readValue(signature);
    var expectedSignatureMap = objectMapper.readerForMapOf(Object.class).readValue(
        TestUtils.getContent("/json/create-app/dso/primaryIncludeSystemSignatureCephContent.json"));
    Assertions.assertThat(signatureMap).isEqualTo(expectedSignatureMap);

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();

    mockServer.verify();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-primary.bpmn")
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

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/create-app/data-factory/findLaboratoryResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("solution-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "WO_CONSIDER"))
        .response("/json/create-app/data-factory/solutionTypeWoConsiderResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("application-type-equal-constant-code")
        .queryParams(Map.of("constantCode", "ADD"))
        .response("/json/create-app/data-factory/applicationTypeResponse.json")
        .build());

    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody("/json/create-app/dso/primaryDenySystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("registration")
        .requestBody("/json/create-app/data-factory/createApplicationPrimaryDenyRequest.json")
        .response("{}")
        .build());

    startProcessInstance(PROCESS_DEFINITION_ID);

    addExpectedVariable("initiator", null);

    // search lab
    assertWaitingActivity(searchLabFormActivityDefinitionKey, "shared-search-lab");

    completeTask(searchLabFormActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-search-lab.json");

    addExpectedCephContent(addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/name-edrpou-prepopulation.json");
    addExpectedCephContent(searchLabFormActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-search-lab.json");

    addExpectedVariable("laboratoryId", labId);

    // add application
    assertWaitingActivity(addApplicationActivityDefinitionKey, "shared-add-application");

    completeTask(addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-application.json");

    addExpectedCephContent(addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-application.json");
    addExpectedCephContent(addBioPhysLaborFactorsActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");
    addExpectedCephContent(addChemFactorsActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-prepopulation.json");
    // add bio phys labor and chem factors
    assertWaitingActivity(addBioPhysLaborFactorsActivityDefinitionKey,
        "shared-add-bio-phys-labor-factors");
    assertWaitingActivity(addChemFactorsActivityDefinitionKey, "shared-add-chem-factors");

    completeTask(addBioPhysLaborFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-bio-phys-labor-factors.json");
    completeTask(addChemFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-chem-factors.json");

    addExpectedCephContent(addBioPhysLaborFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-bio-phys-labor-factors.json");
    addExpectedCephContent(addChemFactorsActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-chem-factors.json");
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
    // add decision include
    assertWaitingActivity(addDecisionDenyActivityDefinitionKey, "shared-add-decision-deny");

    completeTask(addDecisionDenyActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-decision-deny.json");

    addExpectedCephContent(addDecisionDenyActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-decision-deny.json");
    addExpectedCephContent(addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/name-registrationNo-solution-deny-prepopulation.json");

    // add letter data
    assertWaitingActivity(addLetterDataActivityDefinitionKey, "shared-add-letter-data");

    completeTask(addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-letter-data.json");

    addExpectedCephContent(addLetterDataActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-add-letter-data.json");
    addExpectedCephContent(signAppDenyActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-deny_prepopulation.json");

    // sign app deny
    assertWaitingActivity(signAppDenyActivityDefinitionKey, "shared-sign-app-deny");

    completeTask(signAppDenyActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-deny.json");

    addExpectedCephContent(signAppDenyActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-sign-app-deny.json");

    var systemSignatureCephKeyRefVarName = "system_signature_ceph_key";
    var systemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        systemSignatureCephKeyRefVarName;

    addExpectedVariable("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
    addExpectedVariable("sys-var-process-completion-result",
        "Прийнято рішення про залишення без розгляду");

    var signature = cephService.getContent(cephBucketName, systemSignatureCephKey).get();
    var signatureMap = objectMapper.readerForMapOf(Object.class).readValue(signature);
    var expectedSignatureMap = objectMapper.readerForMapOf(Object.class).readValue(
        TestUtils.getContent("/json/create-app/dso/primaryDenySystemSignatureCephContent.json"));
    Assertions.assertThat(signatureMap).isEqualTo(expectedSignatureMap);

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();

    mockServer.verify();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-primary.bpmn")
  public void testValidationError() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";

    var searchLabFormActivityDefinitionKey = "Activity_shared-search-lab";

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("laboratory")
        .resourceId(labId)
        .response(
            "/json/create-app/data-factory/findLaboratoryWithoutAccreditationResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("staff-equal-laboratory-id-count")
        .queryParams(Map.of("laboratoryId", labId))
        .response("[]")
        .build());

    startProcessInstance(PROCESS_DEFINITION_ID);

    addExpectedVariable("initiator", null);
    // search lab
    assertWaitingActivity(searchLabFormActivityDefinitionKey, "shared-search-lab");

    var ex = assertThrows(ValidationException.class,
        () -> completeTask(searchLabFormActivityDefinitionKey,
            "/json/create-app/form-data/Activity_shared-search-lab_without_accreditation.json"));
    assertWaitingActivity(searchLabFormActivityDefinitionKey, "shared-search-lab");
    Assertions.assertThat(ex.getDetails().getErrors()).hasSize(1);
    Assertions.assertThat(ex.getDetails().getErrors())
        .contains(new ErrorDetailDto("Немає ознаки акредитованості", "id", labId));

    mockServer.verify();
  }

  @Test
  @Deployment(resources = "bpmn/create-app-primary.bpmn")
  public void testNoAccreditationFlag() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";

    var searchLabFormActivityDefinitionKey = "Activity_shared-search-lab";
    var addApplicationActivityDefinitionKey = "Activity_shared-add-application";

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("laboratory")
        .resourceId(labId)
        .response(
            "/json/create-app/data-factory/findLaboratoryWithoutAccreditationResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("staff-equal-laboratory-id-count")
        .queryParams(Map.of("laboratoryId", labId))
        .response("[{\"cnt\":1}]")
        .build());

    startProcessInstance(PROCESS_DEFINITION_ID);

    addExpectedVariable("initiator", null);
    // search lab
    assertWaitingActivity(searchLabFormActivityDefinitionKey, "shared-search-lab");

    completeTask(searchLabFormActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-search-lab_without_accreditation.json");

    addExpectedVariable("laboratoryId", labId);
    addExpectedCephContent(searchLabFormActivityDefinitionKey,
        "/json/create-app/form-data/Activity_shared-search-lab_without_accreditation.json");
    addExpectedCephContent(addApplicationActivityDefinitionKey,
        "/json/create-app/form-data/name-edrpou-prepopulation.json");

    assertWaitingActivity(addApplicationActivityDefinitionKey, "shared-add-application");

    mockServer.verify();
  }
}
