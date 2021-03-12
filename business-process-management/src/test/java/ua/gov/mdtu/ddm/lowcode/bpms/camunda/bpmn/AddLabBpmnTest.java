package ua.gov.mdtu.ddm.lowcode.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;

import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.groovy.util.Maps;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;
import ua.gov.mdtu.ddm.lowcode.bpms.it.builder.StubData;

public class AddLabBpmnTest extends BaseBpmnTest {

  @Before
  public void setup() {
    super.init();
  }

  @Test
  @Deployment(resources = {"bpmn/add-lab.bpmn"})
  public void test() throws IOException, URISyntaxException {
    mockDataFactorySearch(StubData.builder()
        .resource("laboratory-equal-edrpou-name-count")
        .response("[]")
        .queryParams(Maps.of("edrpou", "11111111", "name", "labName"))
        .build());

    mockDigitalSignatureSign(StubData.builder()
        .requestBody("/json/add-lab/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    mockDataFactoryCreate(StubData.builder()
        .resource("laboratory")
        .requestBody("/json/add-lab/addLabRequestBody.json")
        .response("{}")
        .build());

    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("add-lab");
    assertThat(processInstance).isStarted();

    //Внести дані про лабораторію
    assertThat(processInstance).isWaitingAt("addLabFormActivity");
    completeTask("addLabFormActivity", "/json/add-lab/Activity_1ne2ryq.json", processInstance.getId());
    //Підписати дані КЕП
    assertThat(processInstance).isWaitingAt("signLabFormActivity");
    completeTask("signLabFormActivity", "/json/add-lab/Activity_0s05qmu.json", processInstance.getId());

    assertThat(processInstance).hasPassed("addLabFormActivity", "signLabFormActivity").isEnded();
  }

}
