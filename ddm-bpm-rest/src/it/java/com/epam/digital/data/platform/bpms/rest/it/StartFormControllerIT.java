package com.epam.digital.data.platform.bpms.rest.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;

class StartFormControllerIT extends BaseIT {

  @Test
  @Deployment(resources = {"bpmn/testStartFormKey.bpmn"})
  @SuppressWarnings("unchecked")
  void shouldGetESignTaskProperties() throws Exception {
    var processDefinitionId = engine.getRepositoryService().createProcessDefinitionQuery()
        .processDefinitionKey("testStartFormKey").singleResult().getId();

    var result = postForObject("api/extended/start-form",
        "{\"processDefinitionIdIn\":[\"" + processDefinitionId + "\",\"nonExistedBP\"]}",
        Map.class);

    assertThat(result).isNotNull().containsEntry(processDefinitionId, "test-form-key");
  }
}
