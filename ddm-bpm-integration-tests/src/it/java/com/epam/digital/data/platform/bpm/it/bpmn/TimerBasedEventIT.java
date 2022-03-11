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
import com.epam.digital.data.platform.bpm.it.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpm.it.util.CamundaAssertionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;

public class TimerBasedEventIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_KEY = "testTimerProcess";

  @Test
  @Deployment(resources = "bpmn/testTimerProcess.bpmn")
  public void testTimerBoundaryEvent() throws JsonProcessingException {
    var processInstanceId = startProcessInstance(PROCESS_DEFINITION_KEY, testUserToken);
    var processInstance = CamundaAssertionUtil.processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_user")
        .formKey("add-userLoan-bp-add-userLoan-test")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName))
        .build());

    executeWaitingJob("Event_time1");

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "bpmn/testTimerProcess.bpmn")
  public void testTimerIntermediateEvent() throws JsonProcessingException {
    var processInstanceId = startProcessInstance(PROCESS_DEFINITION_KEY, testUserToken);
    var processInstance = CamundaAssertionUtil.processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_user")
        .formKey("add-userLoan-bp-add-userLoan-test")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName))
        .build());

    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_user")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("{}")
        .build());

    executeWaitingJob("Event_time2");

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_credit")
        .formKey("add-creditData-bp-add-creditData-test")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName))
        .build());

    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_credit")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("{}")
        .build());

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }
}
