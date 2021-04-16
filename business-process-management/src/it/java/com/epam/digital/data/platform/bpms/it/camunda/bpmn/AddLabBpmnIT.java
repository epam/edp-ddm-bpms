package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import java.io.IOException;
import java.util.HashMap;
import org.apache.groovy.util.Maps;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;

public class AddLabBpmnIT extends BaseBpmnIT {

  @Test
  @Deployment(resources = {"bpmn/add-lab.bpmn"})
  public void test() throws IOException {
    stubDataFactorySearch(StubData.builder()
        .resource("laboratory-equal-edrpou-name-count")
        .queryParams(Maps.of("name", "labName", "edrpou", "11111111"))
        .response("[]")
        .build());

    stubDataFactoryCreate(StubData.builder()
        .resource("laboratory")
        .requestBody("/json/add-lab/addLabRequestBody.json")
        .response("{}")
        .build());

    stubDigitalSignature(StubData.builder()
        .requestBody("/json/add-lab/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    //start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("add-lab", new HashMap<>());
    String processInstanceId = processInstance.getId();

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("addLabFormActivity");
    completeTask("addLabFormActivity", processInstanceId, "/json/add-lab/Activity_1ne2ryq.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("signLabFormActivity");
    completeTask("signLabFormActivity", processInstanceId, "/json/add-lab/Activity_0s05qmu.json");

    //then
    BpmnAwareTests.assertThat(processInstance).hasPassed("addLabFormActivity", "signLabFormActivity").isEnded();
  }

}