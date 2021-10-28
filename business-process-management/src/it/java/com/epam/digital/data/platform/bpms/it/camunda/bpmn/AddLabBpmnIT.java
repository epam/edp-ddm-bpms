package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil.processInstance;

import com.epam.digital.data.platform.bpms.camunda.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpms.camunda.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import java.io.IOException;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class AddLabBpmnIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_KEY = "add-lab";

  @Test
  @Deployment(resources = {"bpmn/add-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testHappyPath() throws IOException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory-equal-edrpou-name-count")
        .queryParams(Maps.of("name", "labName", "edrpou", "77777777"))
        .response("[]")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/add-lab/dso/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    var startFormData = deserializeFormData("/json/add-lab/form-data/start_event.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        testUserToken, startFormData);
    var processInstance = processInstance(processInstanceId);

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("laboratory")
        .headers(Map.of("X-Access-Token", testUserToken, "X-Digital-Signature",
            cephKeyProvider.generateKey("signLabFormActivity", processInstanceId)))
        .requestBody("/json/add-lab/data-factory/addLabRequestBody.json")
        .response("{}")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addLabFormActivity")
        .formKey("add-lab-bp-add-lab")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/add-lab/form-data/addLabFormActivityPrePopulation.json"))
        .expectedVariables(Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addLabFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/add-lab/form-data/addLabFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signLabFormActivity")
        .formKey("shared-sign-lab")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/add-lab/form-data/signLabFormActivityPrePopulation.json"))
        .expectedVariables(Map.of("addLabFormActivity_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signLabFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/add-lab/form-data/signLabFormActivity.json")
        .build());

    addExpectedVariable("signLabFormActivity_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Лабораторія створена");

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/add-lab/dso/digitalSignatureCephContent.json");
    BpmnAwareTests.assertThat(processInstance)
        .hasPassed("addLabFormActivity", "signLabFormActivity").isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/add-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testValidationError() throws IOException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory-equal-edrpou-name-count")
        .queryParams(Maps.of("name", "labName", "edrpou", "77777777"))
        .response("/json/add-lab/data-factory/lab-count.json")
        .build());

    var startFormData = deserializeFormData("/json/add-lab/form-data/start_event.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        testUserToken, startFormData);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addLabFormActivity")
        .formKey("add-lab-bp-add-lab")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/add-lab/form-data/addLabFormActivityPrePopulation.json"))
        .expectedVariables(Map.of("initiator", testUserName))
        .build());
    var result = completeTaskWithError(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addLabFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/add-lab/form-data/addLabFormActivity.json")
        .build());

    var errors = result.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(2);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "name"),
        Map.entry("message", "Дані про цю лабораторію вже присутні"),
        Map.entry("value", "labName"));
    Assertions.assertThat(errors.get(1)).contains(Map.entry("field", "edrpou"),
        Map.entry("message", "Дані про цю лабораторію вже присутні"),
        Map.entry("value", "77777777"));
  }
}
