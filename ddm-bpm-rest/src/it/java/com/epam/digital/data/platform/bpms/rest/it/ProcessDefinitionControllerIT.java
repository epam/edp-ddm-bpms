/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
