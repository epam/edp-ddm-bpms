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

public class UpdateLabBpmnIT extends BaseBpmnIT {

  @Test
  @Deployment(resources = {"bpmn/update-lab.bpmn"})
  public void test() throws IOException {

    stubDataFactoryRead(StubData.builder()
        .resource("laboratory")
        .resourceId("d2943186-0f1f-4a77-9de9-a5a59c07db02")
        .response("/json/update-lab/laboratoryByIdResponse.json")
        .build());

    stubDataFactoryRead(StubData.builder()
        .resource("koatuu")
        .resourceId("92cb1462-ec57-4b87-9e8d-594e0c322996")
        .response("/json/update-lab/koatuuByIdResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("laboratory-equal-edrpou-name-count")
        .queryParams(Maps.of("name", "labName", "edrpou", "23510933"))
        .response("[]")
        .build());

    stubDataFactoryUpdate(StubData.builder()
        .resource("laboratory")
        .resourceId("d2943186-0f1f-4a77-9de9-a5a59c07db02")
        .requestBody("/json/update-lab/updateLabRequestBody.json")
        .response("{}")
        .build());

    stubDigitalSignature(StubData.builder()
        .requestBody("/json/update-lab/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    //start process
    ProcessInstance processInstance = runtimeService
        .startProcessInstanceByKey("update-lab", new HashMap<>());
    String processInstanceId = processInstance.getId();

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("searchLabFormActivity");
    completeTask("searchLabFormActivity", processInstanceId,
        "/json/update-lab/searchLabFormActivity.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("viewLabDataFormActivity");
    completeTask("viewLabDataFormActivity", processInstanceId,
        "/json/update-lab/viewLabDataFormActivity.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("updateLabFormActivity");
    completeTask("updateLabFormActivity", processInstanceId,
        "/json/update-lab/updateLabFormActivity.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("signLabFormActivity");
    completeTask("signLabFormActivity", processInstanceId,
        "/json/update-lab/signLabFormActivity.json");

    //then
    BpmnAwareTests.assertThat(processInstance)
        .hasPassed("searchLabFormActivity", "viewLabDataFormActivity", "updateLabFormActivity")
        .isEnded();
  }
}
