package com.epam.digital.data.platform.bpms.rest.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import java.io.IOException;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;

class ProcessDefinitionControllerIT extends BaseIT {

  @Test
  @Deployment(resources = "/bpmn/testStartFormKey.bpmn")
  void getProcessDefinitionsByParams() throws IOException {

    var result = postForObject("api/extended/process-definition",
        "{\"key\":\"testStartFormKey\"}", DdmProcessDefinitionDto[].class);

    assertThat(result).hasSize(1);
    assertThat(result[0])
        .hasFieldOrProperty("id")
        .hasFieldOrPropertyWithValue("key", "testStartFormKey")
        .hasFieldOrPropertyWithValue("name", "Test Start Form Key")
        .hasFieldOrPropertyWithValue("suspended", false)
        .hasFieldOrPropertyWithValue("formKey", "test-form-key");
  }

  @Test
  @Deployment(resources = "/bpmn/testStartFormKey.bpmn")
  void getProcessDefinitionsByKey() throws IOException {

    var result = getForObject("api/extended/process-definition/key/testStartFormKey",
        DdmProcessDefinitionDto.class);

    assertThat(result)
        .hasFieldOrProperty("id")
        .hasFieldOrPropertyWithValue("key", "testStartFormKey")
        .hasFieldOrPropertyWithValue("name", "Test Start Form Key")
        .hasFieldOrPropertyWithValue("suspended", false)
        .hasFieldOrPropertyWithValue("formKey", "test-form-key");
  }
}
