package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil.processInstance;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.bpms.camunda.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpms.camunda.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import java.io.IOException;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class UpdateDictBpmnIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_ID = "update-dict";

  @Test
  @Deployment(resources = {"bpmn/update-dict.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testHappyPath() throws IOException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("factor-equal-factor-type-name-count")
        .queryParams(Map.of("name", "testName"))
        .response("[]")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/update-dict/dso/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    var processInstanceId = startProcessInstance(PROCESS_DEFINITION_ID, testUserToken);
    var processInstance = processInstance(processInstanceId);

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken, "X-Digital-Signature", cephKeyProvider
            .generateKey("Activity_update-dict-bp-sign-add-name", processInstanceId)))
        .resource("factor")
        .requestBody("/json/update-dict/data-factory/factorRequestBody.json")
        .response("{}")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_update-dict-bp-add-name")
        .formKey("update-dict-bp-add-name")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_update-dict-bp-add-name")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/update-dict/form-data/Activity_update-dict-bp-add-name.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_update-dict-bp-sign-add-name")
        .formKey("update-dict-bp-sign-add-name")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/update-dict/form-data/Activity_update-dict-bp-add-name.json"))
        .expectedVariables(Map.of("Activity_update-dict-bp-add-name_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_update-dict-bp-sign-add-name")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/update-dict/form-data/Activity_update-dict-bp-sign-add-name.json")
        .build());

    addExpectedVariable("Activity_update-dict-bp-sign-add-name_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, "Запис довідника створено");

    BpmnAwareTests.assertThat(processInstance)
        .hasPassed("Activity_update-dict-bp-add-name", "Activity_update-dict-bp-sign-add-name")
        .isEnded();
    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/update-dict/dso/digitalSignatureCephContent.json");
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }
}
