package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import java.io.IOException;
import java.util.LinkedHashMap;
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
        .queryParams(Map.of("edrpou", "77777777", "name", "labName"))
        .build());

    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/add-lab/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    startProcessInstanceWithStartForm();

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .headers(Map.of("X-Digital-Signature",
            cephKeyProvider.generateKey("signLabFormActivity", currentProcessInstanceId)))
        .resource("laboratory")
        .requestBody("/json/add-lab/addLabRequestBody.json")
        .response("{}")
        .build());

    //Внести дані про лабораторію
    addExpectedVariable("initiator", testUserName);
    addExpectedVariable("start_form_ceph_key", START_FORM_CEPH_KEY);
    addExpectedCephContent("addLabFormActivity",
        "/json/add-lab/addLabFormActivityPrePopulation.json");
    assertWaitingActivity("addLabFormActivity", "add-lab-bp-add-lab");
    completeTask("addLabFormActivity", "/json/add-lab/addLabFormActivity.json");
    //Підписати дані КЕП
    addExpectedCephContent("addLabFormActivity",
        "/json/add-lab/addLabFormActivity.json");
    addExpectedCephContent("addLabFormActivity",
        "/json/add-lab/signLabFormActivityPrePopulation.json");
    addExpectedVariable("addLabFormActivity_completer", "testuser");
    assertWaitingActivity("signLabFormActivity", "shared-sign-lab");
    completeTask("signLabFormActivity", "/json/add-lab/signLabFormActivity.json");

    addExpectedCephContent("signLabFormActivity", "/json/add-lab/signLabFormActivity.json");

    assertThat(currentProcessInstance).hasPassed("addLabFormActivity", "signLabFormActivity")
        .isEnded();

    assertSystemSignature("system_signature_ceph_key",
        "/json/add-lab/digitalSignatureCephContent.json");
    assertCephContent();

    mockServer.verify();
  }

  protected void startProcessInstanceWithStartForm() {
    var data = new LinkedHashMap<String, Object>();
    data.put("subjectType", "LEGAL");
    data.put("edrpou", "77777777");
    data.put("subject", Map.of("subjectId", "activeSubject"));

    startProcessInstanceWithStartForm("add-lab", data);
  }
}
