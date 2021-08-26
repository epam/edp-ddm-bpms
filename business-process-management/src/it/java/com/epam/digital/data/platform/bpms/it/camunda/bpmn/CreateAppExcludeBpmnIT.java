package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CreateAppExcludeBpmnIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_ID = "create-app-exclude";

  @Test
  @Deployment(resources = {"bpmn/create-app-exclude.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testHappyPath() throws IOException {
    var laboratoryId = "d2943186-0f1f-4a77-9de9-a5a59c07db02";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Maps.of("laboratoryId", laboratoryId))
        .response("/json/create-app-exclude/lastLaboratorySolutionResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "ADD"))
        .response("/json/create-app-exclude/applicationTypeEqualConstantCodeAddResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "ADD"))
        .response("/json/create-app-exclude/solutionTypeEqualConstantCodeAddResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(laboratoryId)
        .response("/json/create-app-exclude/laboratoryByIdResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "EXCLUDE"))
        .response("/json/create-app-exclude/solutionTypeEqualConstantCodeResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "EXCLUDE"))
        .response("/json/create-app-exclude/applicationTypeEqualConstantCodeResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("registration")
        .requestBody("/json/create-app-exclude/addRegistrationBody.json")
        .response("{}")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-app-exclude/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    var processInstanceId = startProcessInstanceWithFormAndGetId(laboratoryId);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("addApplicationFormActivity");
    completeTask("addApplicationFormActivity", processInstanceId,
        "/json/create-app-exclude/addApplicationFormActivity.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("checkComplianceFormActivity");
    completeTask("checkComplianceFormActivity", processInstanceId,
        "/json/create-app-exclude/checkComplianceFormActivity.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("addDecisionExcludeFormActivity");
    completeTask("addDecisionExcludeFormActivity", processInstanceId,
        "/json/create-app-exclude/addDecisionExcludeFormActivity.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("addLetterDataFormForExclusionActivity");
    completeTask("addLetterDataFormForExclusionActivity", processInstanceId,
        "/json/create-app-exclude/addLetterDataFormForExclusionActivity.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("signAppExcludeFormActivity");
    completeTask("signAppExcludeFormActivity", processInstanceId,
        "/json/create-app-exclude/signAppExcludeFormActivity.json");

    //then
    BpmnAwareTests.assertThat(processInstance)
        .hasPassed("addApplicationFormActivity", "checkComplianceFormActivity",
            "addDecisionExcludeFormActivity", "addLetterDataFormForExclusionActivity",
            "signAppExcludeFormActivity")
        .isEnded();

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/create-app-exclude/digitalSignatureCephContent.json");
  }

  @Test
  @Deployment(resources = {"bpmn/create-app-exclude.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testPathWithMistakes() throws IOException {
    var laboratoryId = "d2943186-0f1f-4a77-9de9-a5a59c07db02";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Maps.of("laboratoryId", laboratoryId))
        .response("/json/create-app-exclude/lastLaboratorySolutionResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "ADD"))
        .response("/json/create-app-exclude/applicationTypeEqualConstantCodeAddResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "ADD"))
        .response("/json/create-app-exclude/solutionTypeEqualConstantCodeAddResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(laboratoryId)
        .response("/json/create-app-exclude/laboratoryByIdResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "WO_CONSIDER"))
        .response("/json/create-app-exclude/solutionTypeEqualConstantCodeWoConsiderResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "EXCLUDE"))
        .response("/json/create-app-exclude/applicationTypeEqualConstantCodeResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("registration")
        .requestBody("/json/create-app-exclude/addRegistrationNoConsiderBody.json")
        .response("{}")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-app-exclude/digitalSignatureNoConsiderRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    //start process
    var processInstanceId = startProcessInstanceWithFormAndGetId(laboratoryId);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("addApplicationFormActivity");
    completeTask("addApplicationFormActivity", processInstanceId,
        "/json/create-app-exclude/addApplicationFormActivity.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("checkComplianceFormActivity");
    completeTask("checkComplianceFormActivity", processInstanceId,
        "/json/create-app-exclude/noConsiderCheckComplianceFormActivity.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("addDecisionDenyFormActivity");
    completeTask("addDecisionDenyFormActivity", processInstanceId,
        "/json/create-app-exclude/addDecisionDenyFormActivity.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("addLetterDataFormForDenyActivity");
    completeTask("addLetterDataFormForDenyActivity", processInstanceId,
        "/json/create-app-exclude/addLetterDataFormForDenyActivity.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("signAppDenyFormActivity");
    completeTask("signAppDenyFormActivity", processInstanceId,
        "/json/create-app-exclude/signAppDenyFormActivity.json");

    //then
    BpmnAwareTests.assertThat(processInstance)
        .hasPassed("addApplicationFormActivity", "checkComplianceFormActivity",
            "addDecisionDenyFormActivity", "addLetterDataFormForDenyActivity",
            "signAppDenyFormActivity")
        .isEnded();

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/create-app-exclude/digitalSignatureNoConsiderCephContent.json");
  }

  @Test
  @Deployment(resources = "bpmn/create-app-exclude.bpmn")
  public void testValidationError() throws IOException {
    var laboratoryId = "d2943186-0f1f-4a77-9de9-a5a59c07db02";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Maps.of("laboratoryId", laboratoryId))
        .response("/json/create-app-exclude/lastLaboratorySolutionDenyResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "ADD"))
        .response("/json/create-app-exclude/applicationTypeEqualConstantCodeAddResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "ADD"))
        .response("/json/create-app-exclude/solutionTypeEqualConstantCodeAddResponse.json")
        .build());

    var resultMap = startProcessInstanceForError(laboratoryId);

    var errors = resultMap.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "laboratory"),
        Map.entry("message", "Заява на видалення вже створена"),
        Map.entry("value", laboratoryId));
  }

  private String startProcessInstanceWithFormAndGetId(String labId) throws JsonProcessingException {
    return startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_ID, testUserToken,
        startFormData(labId));
  }

  private FormDataDto startFormData(String labId) {
    var data = new LinkedHashMap<String, Object>();
    data.put("laboratory", Map.of("laboratoryId", labId));
    return FormDataDto.builder().data(data).build();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Map<String, List<Map<String, String>>>> startProcessInstanceForError(
      String labId) throws JsonProcessingException {
    var resultMap = startProcessInstanceWithStartForm(PROCESS_DEFINITION_ID, testUserToken,
        startFormData(labId));
    return (Map<String, Map<String, List<Map<String, String>>>>) resultMap;
  }
}
