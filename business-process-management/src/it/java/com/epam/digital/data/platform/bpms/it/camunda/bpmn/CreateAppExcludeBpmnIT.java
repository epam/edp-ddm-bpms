package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import java.io.IOException;
import java.util.HashMap;
import org.apache.groovy.util.Maps;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CreateAppExcludeBpmnIT extends BaseBpmnIT {

  @Test
  @Deployment(resources = {"bpmn/create-app-exclude.bpmn"})
  public void testHappyPath() throws IOException {

    stubDataFactoryRead(StubData.builder()
        .resource("laboratory")
        .resourceId("d2943186-0f1f-4a77-9de9-a5a59c07db02")
        .response("/json/create-app-exclude/laboratoryByIdResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("solution-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "EXCLUDE"))
        .response("/json/create-app-exclude/solutionTypeEqualConstantCodeResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("application-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "EXCLUDE"))
        .response("/json/create-app-exclude/applicationTypeEqualConstantCodeResponse.json")
        .build());

    stubDataFactoryCreate(StubData.builder()
        .resource("registration")
        .requestBody("/json/create-app-exclude/addRegistrationBody.json")
        .response("{}")
        .build());

    stubDigitalSignature(StubData.builder()
        .requestBody("/json/create-app-exclude/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    //start process
    ProcessInstance processInstance = runtimeService
        .startProcessInstanceByKey("create-app-exclude", new HashMap<>());
    String processInstanceId = processInstance.getId();

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("searchLabFormActivity");
    completeTask("searchLabFormActivity", processInstanceId,
        "/json/create-app-exclude/searchLabFormActivity.json");

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
        .hasPassed("searchLabFormActivity", "addApplicationFormActivity",
            "checkComplianceFormActivity", "addDecisionExcludeFormActivity",
            "addLetterDataFormForExclusionActivity", "signAppExcludeFormActivity")
        .isEnded();
  }

  @Test
  public void testPathWithMistakes() throws IOException {
    stubDataFactoryRead(StubData.builder()
        .resource("laboratory")
        .resourceId("d2943186-0f1f-4a77-9de9-a5a59c07db02")
        .response("/json/create-app-exclude/laboratoryByIdResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("solution-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "WO_CONSIDER"))
        .response("/json/create-app-exclude/solutionTypeEqualConstantCodeWoConsiderResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("application-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "EXCLUDE"))
        .response("/json/create-app-exclude/applicationTypeEqualConstantCodeResponse.json")
        .build());

    stubDataFactoryCreate(StubData.builder()
        .resource("registration")
        .requestBody("/json/create-app-exclude/addRegistrationNoConsiderBody.json")
        .response("{}")
        .build());

    stubDigitalSignature(StubData.builder()
        .requestBody("/json/create-app-exclude/digitalSignatureNoConsiderRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    //start process
    ProcessInstance processInstance = runtimeService
        .startProcessInstanceByKey("create-app-exclude", new HashMap<>());
    String processInstanceId = processInstance.getId();

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("searchLabFormActivity");
    completeTask("searchLabFormActivity", processInstanceId,
        "/json/create-app-exclude/searchLabFormActivity.json");

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
        .hasPassed("searchLabFormActivity", "addApplicationFormActivity",
            "checkComplianceFormActivity", "addDecisionDenyFormActivity",
            "addLetterDataFormForDenyActivity", "signAppDenyFormActivity")
        .isEnded();
  }
}
