package ua.gov.mdtu.ddm.lowcode.bpms.delegate.connector;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.bpmn.BaseBpmnTest;
import ua.gov.mdtu.ddm.lowcode.bpms.it.builder.StubData;

@RunWith(MockitoJUnitRunner.class)
public class DataFactoryConnectorBatchReadDelegateTest extends BaseBpmnTest {

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorBatchReadDelegate.bpmn"})
  public void testBatchRead() throws IOException {
    String chemResearchId = "7074945f-e088-446b-8c28-325aca4f423f";
    String physResearchId = "0b3c9f55-ba50-4d87-970a-bfbb8e31adeb";

    mockDataFactoryGet(StubData.builder()
        .resource("research")
        .resourceId(chemResearchId)
        .response("/json/researchResponseChem.json")
        .build());
    mockDataFactoryGet(StubData.builder()
        .resource("research")
        .resourceId(physResearchId)
        .response("/json/researchResponsePhys.json")
        .build());

    //start process
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("test-batch-read",
        Map.of("resourceIds", List.of(chemResearchId, physResearchId)));

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }
}