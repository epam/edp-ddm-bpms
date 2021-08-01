package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import java.io.IOException;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class AddLabBpmnTest extends BaseBpmnTest {

  @Test
  @Deployment(resources = {"bpmn/add-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void test() throws IOException {
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory-equal-edrpou-name-count")
        .response("[]")
        .queryParams(Map.of("edrpou", "11111111", "name", "labName"))
        .build());

    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/add-lab/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    startProcessInstance("add-lab");

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .headers(Map.of("X-Digital-Signature",
            cephKeyProvider.generateKey("signLabFormActivity", currentProcessInstanceId)))
        .resource("laboratory")
        .requestBody("/json/add-lab/addLabRequestBody.json")
        .response("{}")
        .build());

    addExpectedVariable("initiator", null);
    //Внести дані про лабораторію
    assertWaitingActivity("addLabFormActivity", "add-lab-bp-add-lab");
    completeTask("addLabFormActivity", "/json/add-lab/Activity_1ne2ryq.json");
    //Підписати дані КЕП
    addExpectedVariable("addLabFormActivity_completer", "testuser");
    assertWaitingActivity("signLabFormActivity", "shared-sign-lab");
    completeTask("signLabFormActivity", "/json/add-lab/Activity_0s05qmu.json");

    assertThat(currentProcessInstance).hasPassed("addLabFormActivity", "signLabFormActivity")
        .isEnded();

    assertSystemSignature("system_signature_ceph_key",
        "/json/add-lab/digitalSignatureCephContent.json");

    mockServer.verify();
  }
}
