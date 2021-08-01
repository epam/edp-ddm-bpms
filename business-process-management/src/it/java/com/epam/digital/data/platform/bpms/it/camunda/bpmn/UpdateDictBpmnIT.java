package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import java.io.IOException;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.http.HttpMethod;

@Deployment(resources = {"bpmn/update-dict.bpmn", "bpmn/system-signature-bp.bpmn"})
public class UpdateDictBpmnIT extends BaseBpmnIT {

  @Test
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
        .requestBody("/json/update-dict/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    //start process
    var processInstance = runtimeService.startProcessInstanceByKey("update-dict");
    var processInstanceId = processInstance.getId();
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken, "X-Digital-Signature", cephKeyProvider
            .generateKey("Activity_update-dict-bp-sign-add-name", processInstanceId)))
        .resource("factor")
        .requestBody("/json/update-dict/factorRequestBody.json")
        .response("{}")
        .build());

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("Activity_update-dict-bp-add-name");
    completeTask("Activity_update-dict-bp-add-name", processInstanceId,
        "/json/update-dict/Activity_update-dict-bp-add-name.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("Activity_update-dict-bp-sign-add-name");
    completeTask("Activity_update-dict-bp-sign-add-name", processInstanceId,
        "/json/update-dict/Activity_update-dict-bp-sign-add-name.json");

    //then
    BpmnAwareTests.assertThat(processInstance)
        .hasPassed("Activity_update-dict-bp-add-name", "Activity_update-dict-bp-sign-add-name")
        .isEnded();

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/update-dict/digitalSignatureCephContent.json");
  }
}
