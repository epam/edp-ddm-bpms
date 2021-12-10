package com.epam.digital.data.platform.bpms.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class ProcessStartTimeIT extends BaseIT {

  @Deployment(resources = "/bpmn/testStartTimeVariableInExtendedRestCall.bpmn")
  @Test
  public void testStartTimeVariableInExtendedRestCall() throws JsonProcessingException {
    var startedProcessInstance = postForObject(
        "api/process-definition/key/testStartTimeVariableInExtendedRestCall/start", "{}", Map.class);
    var processInstanceId = (String) startedProcessInstance.get("id");

    var result = postForObject("api/extended/process-instance",
        "{\"processInstanceIds\":[\"" + processInstanceId + "\"]}", DdmProcessInstanceDto[].class);

    assertThat(result).hasSize(1);
    assertThat(result[0].getStartTime()).isNotNull();
  }
}
