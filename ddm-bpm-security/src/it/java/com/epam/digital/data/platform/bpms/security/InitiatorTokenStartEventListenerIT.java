package com.epam.digital.data.platform.bpms.security;

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

    assertThat((Boolean) result.get("ended")).isTrue();
  }
}
