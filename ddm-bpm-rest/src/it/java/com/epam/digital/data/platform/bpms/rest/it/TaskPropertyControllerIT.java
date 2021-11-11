package com.epam.digital.data.platform.bpms.rest.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;

class TaskPropertyControllerIT extends BaseIT {

  @Test
  @Deployment(resources = {"bpmn/testTaskProperty.bpmn"})
  @SuppressWarnings("unchecked")
  void shouldGetESignTaskProperties() throws Exception {
    var processes = postForObject("api/process-definition/key/testTaskProperty_key/start", "",
        Map.class);

    var processId = (String) processes.get("id");
    var tasks = engine.getTaskService().createTaskQuery().processInstanceId(processId)
        .list();

    var result = (Map<String, String>) getForObject(
        "api/extended/task/" + tasks.get(0).getId() + "/extension-element/property", Map.class);

    assertThat(result).isNotNull()
        .containsEntry("eSign", "true");
  }
}
