package com.epam.digital.data.platform.bpms.rest.it;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class StartFormControllerIT extends BaseIT {

  @Test
  @Deployment(resources = {"bpmn/testStartFormKey.bpmn"})
  public void shouldGetESignTaskProperties() throws Exception {
    var processDefinitionId = engine.getRepositoryService().createProcessDefinitionQuery()
        .processDefinitionKey("testStartFormKey").singleResult().getId();

    var result = postForObject("api/extended/start-form",
        "{\"processDefinitionIdIn\":[\"" + processDefinitionId + "\",\"nonExistedBP\"]}",
        Map.class);

    assertThat(result).isNotNull();
    assertThat(result.get(processDefinitionId)).isEqualTo("test-form-key");
  }
}
