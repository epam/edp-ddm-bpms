package com.epam.digital.data.platform.bpms.rest.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.DdmProcessInstanceStatus;
import java.io.IOException;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;

class ProcessInstanceControllerIT extends BaseIT {

  @Test
  @Deployment(resources = "/bpmn/testHistoryProcessInstances.bpmn")
  void getProcessInstances_completed() throws IOException {
    postForObject("api/process-definition/key/testHistoryProcessInstances/start", "", Map.class);

    var result = postForObject("api/extended/process-instance",
        "{\"rootProcessInstances\":true, \"finished\":true}", DdmProcessInstanceDto[].class);

    assertThat(result).isEmpty();
  }

  @Test
  @Deployment(resources = "/bpmn/testPendingProcessInstance.bpmn")
  void getProcessInstances_pending() throws IOException {
    postForObject("api/process-definition/key/testPendingProcessInstance/start", "", Map.class);

    var result = postForObject("api/extended/process-instance",
        "{\"rootProcessInstances\":true,\"sortBy\":\"startTime\",\"sortOrder\":\"asc\"}",
        DdmProcessInstanceDto[].class);

    assertThat(result).hasSize(1);
    assertThat(result[0])
        .hasFieldOrProperty("id")
        .hasFieldOrProperty("processDefinitionId")
        .hasFieldOrPropertyWithValue("processDefinitionName", "Test Pending Process Instance")
        .hasFieldOrProperty("startTime")
        .hasFieldOrPropertyWithValue("state", DdmProcessInstanceStatus.PENDING);
  }
}
