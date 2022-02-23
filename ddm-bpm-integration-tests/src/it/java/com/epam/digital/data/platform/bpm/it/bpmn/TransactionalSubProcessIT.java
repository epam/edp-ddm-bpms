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

package com.epam.digital.data.platform.bpm.it.bpmn;

import com.epam.digital.data.platform.bpm.it.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpm.it.util.CamundaAssertionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.junit.Test;

public class TransactionalSubProcessIT extends BaseBpmnIT {

  @Test
  public void testCancelPath() throws JsonProcessingException {
    var processDefKey = "test-cancel-event";

    var startProcessInstanceDto = new StartProcessInstanceDto();
    var variableValueDto = new VariableValueDto();
    variableValueDto.setType("Boolean");
    variableValueDto.setValue(true);
    startProcessInstanceDto.setVariables(Map.of("isCanceled", variableValueDto));

    var processInstanceId = startProcessInstance(processDefKey, startProcessInstanceDto,
        testUserToken);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(processDefKey)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_cancel_path")
        .assignee("testuser")
        .build());
  }

  @Test
  public void testSuccessPath() throws JsonProcessingException {
    var processDefKey = "test-cancel-event";

    var startProcessInstanceDto = new StartProcessInstanceDto();
    var variableValueDto = new VariableValueDto();
    variableValueDto.setType("Boolean");
    variableValueDto.setValue(false);
    startProcessInstanceDto.setVariables(Map.of("isCanceled", variableValueDto));

    var processInstanceId = startProcessInstance(processDefKey, startProcessInstanceDto,
        testUserToken);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(processDefKey)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_successful_path")
        .assignee("testuser")
        .build());
  }
}
