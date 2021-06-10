package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import java.io.IOException;
import org.apache.groovy.util.Maps;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class AddLabBpmnTest extends BaseBpmnTest {

  @Test
  @Deployment(resources = {"bpmn/add-lab.bpmn"})
  public void test() throws IOException {
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("laboratory-equal-edrpou-name-count")
        .response("[]")
        .queryParams(Maps.of("edrpou", "11111111", "name", "labName"))
        .build());

    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .requestBody("/json/add-lab/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("laboratory")
        .requestBody("/json/add-lab/addLabRequestBody.json")
        .response("{}")
        .build());

    startProcessInstance("add-lab");

    addExpectedVariable("initiator", null);
    //Внести дані про лабораторію
    assertWaitingActivity("addLabFormActivity", "add-lab-bp-add-lab");
    completeTask("addLabFormActivity", "/json/add-lab/Activity_1ne2ryq.json");
    //Підписати дані КЕП
    assertWaitingActivity("signLabFormActivity", "shared-sign-lab");
    completeTask("signLabFormActivity", "/json/add-lab/Activity_0s05qmu.json");

    assertThat(currentProcessInstance).hasPassed("addLabFormActivity", "signLabFormActivity")
        .isEnded();

    mockServer.verify();
  }
}
