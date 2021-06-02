package com.epam.digital.data.platform.bpms.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class JuelFunctionsIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/initiator_juel_function.bpmn")
  public void testInitiatorAccessToken() throws JsonProcessingException {
    var result = postForObject("api/process-definition/key/initiator_juel_function/start",
        "{}", Map.class);

    var vars = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId((String) result.get("id")).list();

    assertThat(vars).hasSize(3);
    var historicVarNames = vars.stream()
        .map(HistoricVariableInstance::getName)
        .collect(Collectors.toList());
    assertThat(historicVarNames)
        .hasSize(3)
        .contains("initiator", "const_dataFactoryBaseUrl", "elInitiator");
  }
}
