package com.epam.digital.data.platform.bpms.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class InitiatorTokenStartEventListenerIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/initiator_access_token.bpmn")
  public void testInitiatorAccessToken() throws JsonProcessingException {
    var result = postForObject("api/process-definition/key/initiator_access_token/start",
        "{}", Map.class);

    var vars = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId((String) result.get("id")).list();

    assertThat(vars).hasSize(1);
    assertThat(vars.get(0).getName()).isEqualTo("const_dataFactoryBaseUrl");
  }
}
