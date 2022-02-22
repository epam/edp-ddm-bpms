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
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class EmbeddedSubprocessIT extends BaseBpmnIT {

  @Test
  @Deployment(resources = {"bpmn/3typessubprocess.bpmn"})
  public void happyPath() throws JsonProcessingException {
    var processDefinitionKey = "embedded";

    var processInstanceId = startProcessInstance(processDefinitionKey, testUserToken);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(processDefinitionKey)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_0az53x3")
        .formKey("add-application")
        .assignee(testUserName)
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_0az53x3")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/embedded-bp/add-application.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(processDefinitionKey)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_0u4619o")
        .formKey("add-appgood")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/embedded-bp/add-application.json"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_0u4619o")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/embedded-bp/add-appgood.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(processDefinitionKey)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_0k9p732")
        .formKey("add-appreviewsecond")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/embedded-bp/add-appgood.json"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_0k9p732")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/embedded-bp/add-appreviewsecond.json")
        .build());

    assertThat(processInstance).isEnded();
  }
}
