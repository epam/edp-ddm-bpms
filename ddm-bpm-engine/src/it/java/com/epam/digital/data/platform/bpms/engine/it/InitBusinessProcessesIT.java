package com.epam.digital.data.platform.bpms.engine.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.bpms.engine.config.CamundaProperties;
import java.util.Map;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class InitBusinessProcessesIT extends BaseIT {

  @Autowired
  private CamundaProperties camundaProperties;

  @Test
  @Deployment(resources = {"bpmn/testInitSystemVariablesProcess.bpmn"})
  void shouldInitDataFactoryBaseUrlDuringDeploy() {
    String varDataFactoryBaseUrl = "const_dataFactoryBaseUrl";

    ProcessInstance process = runtimeService
        .startProcessInstanceByKey("testInitSystemVariablesProcess_key", "1");

    Map<String, Object> variables = runtimeService.getVariables(process.getId());
    String dataFactoryBaseUrl = (String) variables.get(varDataFactoryBaseUrl);
    assertThat(dataFactoryBaseUrl).isNotNull()
        .isEqualTo(camundaProperties.getSystemVariables().get(varDataFactoryBaseUrl));
  }
}
