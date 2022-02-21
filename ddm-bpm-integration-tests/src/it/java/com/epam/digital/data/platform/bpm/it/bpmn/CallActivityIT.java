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

import static com.epam.digital.data.platform.bpm.it.util.CamundaAssertionUtil.processInstance;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpm.it.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpm.it.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpm.it.util.CamundaAssertionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class CallActivityIT extends BaseBpmnIT {

  @Test
  @Deployment(resources = "bpmn/callactivityfinal.bpmn")
  public void happyPath() throws JsonProcessingException {

    var processDefKey = "create-order";
    var data = deserializeFormData("/json/call-activity/add-data.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(processDefKey, testUserToken,
        data);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(processDefKey)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_03ohi65")
        .formKey("add-order-bp-add-order-test")
        .assignee("testuser")
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_03ohi65")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/call-activity/add-order-bp-add-order-test.json")
        .build());

    var callActivityId = "Activity_2";
    var callActivityProcessDefinitionKey = "order-confirm";
    assertThat(processInstance(processInstanceId)).isWaitingAt(callActivityId);
    var task = taskService.createTaskQuery().taskDefinitionKey("Activity_3").singleResult();
    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(callActivityProcessDefinitionKey)
        .processInstanceId(task.getProcessInstanceId())
        .activityDefinitionId("Activity_3")
        .formKey("add-order-bp-order-confirm-test")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/call-activity/add-order-bp-order-confirm-test-pre-population.json"))
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(task.getProcessInstanceId())
        .activityDefinitionId("Activity_3")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/call-activity/add-order-bp-order-confirm-test.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(processDefKey)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_4")
        .formKey("add-order-bp-view-order-test")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/call-activity/add-order-bp-view-order-test-pre-population.json"))
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_4")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/call-activity/add-order-bp-view-order-test.json")
        .build());

    assertThat(processInstance).isEnded();
  }
}
