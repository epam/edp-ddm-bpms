package com.epam.digital.data.platform.bpms.delegate.connector;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;

import com.epam.digital.data.platform.bpms.camunda.bpmn.BaseBpmnTest;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;

@RunWith(MockitoJUnitRunner.class)
public class DataFactoryConnectorBatchReadDelegateTest extends BaseBpmnTest {

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorBatchReadDelegate.bpmn"})
  public void testBatchRead() throws IOException {
    String chemResearchId = "7074945f-e088-446b-8c28-325aca4f423f";
    String physResearchId = "0b3c9f55-ba50-4d87-970a-bfbb8e31adeb";

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("research")
        .resourceId(chemResearchId)
        .response("/json/researchResponseChem.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
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