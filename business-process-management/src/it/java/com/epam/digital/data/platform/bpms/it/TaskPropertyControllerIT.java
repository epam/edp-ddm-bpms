package com.epam.digital.data.platform.bpms.it;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class TaskPropertyControllerIT extends BaseIT {

  @Test
  @Deployment(resources = {"bpmn/testTaskProperty.bpmn"})
  public void shouldGetESignTaskProperties() throws Exception {
    var processes = postForObject("api/process-definition/key/testTaskProperty_key/start", "",
        Map.class);

    var processId = (String) processes.get("id");
    var tasks = engine.getTaskService().createTaskQuery().processInstanceId(processId)
        .list();

    var result = getForObject(
        "api/extended/task/" + tasks.get(0).getId() + "/extension-element/property", Map.class);

    assertThat(result).isNotNull();
    assertThat(result.get("eSign")).isEqualTo("true");
  }
}
