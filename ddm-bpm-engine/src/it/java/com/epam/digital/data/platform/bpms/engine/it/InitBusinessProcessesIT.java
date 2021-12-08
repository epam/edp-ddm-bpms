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

package com.epam.digital.data.platform.bpms.engine.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.bpms.engine.config.CamundaProperties;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessStartTimeVariable;
import java.time.LocalDateTime;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class InitBusinessProcessesIT extends BaseIT {

  @Autowired
  private CamundaProperties camundaProperties;

  @Test
  @Deployment(resources = {"bpmn/testInitSystemVariablesProcess.bpmn"})
  void shouldInitSystemVariablesDuringDeploy() {
    var varDataFactoryBaseUrl = "const_dataFactoryBaseUrl";

    var process = runtimeService.startProcessInstanceByKey("testInitSystemVariablesProcess_key",
        "1");

    var variables = runtimeService.getVariables(process.getId());
    var dataFactoryBaseUrl = (String) variables.get(varDataFactoryBaseUrl);
    var startTime = (LocalDateTime) variables.get(
        ProcessStartTimeVariable.SYS_VAR_PROCESS_START_TIME);
    assertThat(dataFactoryBaseUrl).isNotNull()
        .isEqualTo(camundaProperties.getSystemVariables().get(varDataFactoryBaseUrl));
    assertThat(startTime).isNotNull();
  }
}
