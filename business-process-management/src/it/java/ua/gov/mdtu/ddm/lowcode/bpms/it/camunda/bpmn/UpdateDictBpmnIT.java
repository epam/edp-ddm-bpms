package ua.gov.mdtu.ddm.lowcode.bpms.it.camunda.bpmn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Ignore;
import org.junit.Test;
import ua.gov.mdtu.ddm.lowcode.bpms.it.builder.StubData;

@Deployment(resources = {"bpmn/update-dict.bpmn"})
public class UpdateDictBpmnIT extends BaseBpmnIT {

  @Test
  @Ignore //issues with cyrillic in json using 'mvn install'
  public void testHappyPath() throws IOException {
    stubDataFactorySearch(StubData.builder()
        .resource("factor-equal-factor-type-name-count")
        .queryParams(Map.of("name", "testName"))
        .response("[]")
        .build());

    stubDigitalSignature(StubData.builder()
        .requestBody("/json/update-dict/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryCreate(StubData.builder()
        .resource("factor")
        .requestBody("/json/update-dict/factorRequestBody.json")
        .response("{}")
        .build());

    //start process
    ProcessInstance processInstance = runtimeService
        .startProcessInstanceByKey("update-dict", new HashMap<>());
    String processInstanceId = processInstance.getId();

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
  }
}
