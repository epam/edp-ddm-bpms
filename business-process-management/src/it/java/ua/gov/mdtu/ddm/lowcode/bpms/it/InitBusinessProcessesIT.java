package ua.gov.mdtu.ddm.lowcode.bpms.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.config.CamundaProperties;

public class InitBusinessProcessesIT extends BaseIT {

  @Autowired
  private CamundaProperties camundaProperties;

  @Test
  @Deployment(resources = {"bpmn/testInitSystemVariablesProcess.bpmn"})
  public void shouldInitDataFactoryBaseUrlDuringDeploy() {
    String varDataFactoryBaseUrl = "dataFactoryBaseUrl";

    ProcessInstance process = runtimeService
        .startProcessInstanceByKey("testInitSystemVariablesProcess_key", "1");

    Map<String, Object> variables = runtimeService.getVariables(process.getId());
    String dataFactoryBaseUrl = (String) variables.get(varDataFactoryBaseUrl);
    assertThat(dataFactoryBaseUrl).isNotNull();
    assertThat(dataFactoryBaseUrl)
        .isEqualTo(camundaProperties.getSystemVariables().get(varDataFactoryBaseUrl));
  }
}
