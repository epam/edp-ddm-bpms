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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;

import com.epam.digital.data.platform.bpm.it.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpm.it.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpm.it.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;

public class BpsInteractionWithMessagesIT extends BaseBpmnIT {

  private static final String USER_PROCESS_DEFINITION_KEY = "processThatStartAnotherProcessAndWaitsForResponse";
  private static final String MESSAGE_PROCESS_DEFINITION_KEY = "processThatIsStartedByAnotherProcessAndSendsResponse";

  @Test
  @Deployment(resources = "bpmn/bps-interaction-with-messages.bpmn")
  public void test() throws JsonProcessingException {
    var userProcessInstanceId = startProcessInstance(USER_PROCESS_DEFINITION_KEY, testUserToken);
    var userProcessInstance = processInstance(userProcessInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(USER_PROCESS_DEFINITION_KEY)
        .processInstanceId(userProcessInstanceId)
        .activityDefinitionId("eventBasedGateway")
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());

    var firstMessageProcessInstance = BpmnAwareTests.runtimeService().createProcessInstanceQuery()
        .processDefinitionKey(MESSAGE_PROCESS_DEFINITION_KEY)
        .singleResult();
    var firstMessageProcessInstanceId = firstMessageProcessInstance.getId();

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(MESSAGE_PROCESS_DEFINITION_KEY)
        .processInstanceId(firstMessageProcessInstanceId)
        .activityDefinitionId("user_form_1")
        .formKey("user-form-1")
        .candidateUsers(List.of("second_process_user", testUserName))
        .build());

    var claimUrl = String.format("api/task/%s/claim", task("user_form_1").getId());
    var claimBody = String.format("{\"userId\":\"%s\"}", testUserName);
    postForNoContent(claimUrl, claimBody, testUserToken);

    completeTask(CompleteActivityDto.builder()
        .processInstanceId(firstMessageProcessInstanceId)
        .activityDefinitionId("user_form_1")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("{\"data\":{\"ok\":false}}")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(MESSAGE_PROCESS_DEFINITION_KEY)
        .processInstanceId(firstMessageProcessInstanceId)
        .activityDefinitionId("user_form_3")
        .formKey("user-form-3")
        .assignee(testUserName)
        .build());

    completeTask(CompleteActivityDto.builder()
        .processInstanceId(firstMessageProcessInstanceId)
        .activityDefinitionId("user_form_3")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("{\"data\":{\"notOkMessageVariable\":\"notOkMessageVariableValue\"}}")
        .build());

    BpmnAwareTests.assertThat(firstMessageProcessInstance).isEnded();

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(USER_PROCESS_DEFINITION_KEY)
        .processInstanceId(userProcessInstanceId)
        .activityDefinitionId("user_form_5")
        .formKey("user-form-5")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(FormDataDto.builder()
            .data(new LinkedHashMap<>(Map.of("notOkMessageVariable", "notOkMessageVariableValue")))
            .build())
        .build());

    completeTask(CompleteActivityDto.builder()
        .processInstanceId(userProcessInstanceId)
        .activityDefinitionId("user_form_5")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(USER_PROCESS_DEFINITION_KEY)
        .processInstanceId(userProcessInstanceId)
        .activityDefinitionId("eventBasedGateway")
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());

    var secondMessageProcessInstance = BpmnAwareTests.runtimeService().createProcessInstanceQuery()
        .processDefinitionKey(MESSAGE_PROCESS_DEFINITION_KEY)
        .singleResult();
    var secondMessageProcessInstanceId = secondMessageProcessInstance.getId();

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(MESSAGE_PROCESS_DEFINITION_KEY)
        .processInstanceId(secondMessageProcessInstanceId)
        .activityDefinitionId("user_form_1")
        .formKey("user-form-1")
        .candidateUsers(List.of("second_process_user"))
        .build());

    completeTask(CompleteActivityDto.builder()
        .processInstanceId(secondMessageProcessInstanceId)
        .activityDefinitionId("user_form_1")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("{\"data\":{\"ok\":true}}")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(MESSAGE_PROCESS_DEFINITION_KEY)
        .processInstanceId(secondMessageProcessInstanceId)
        .activityDefinitionId("user_form_2")
        .formKey("user-form-2")
        .assignee(testUserName)
        .build());

    completeTask(CompleteActivityDto.builder()
        .processInstanceId(secondMessageProcessInstanceId)
        .activityDefinitionId("user_form_2")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("{\"data\":{\"okMessageVariable\":\"okMessageVariableValue\"}}")
        .build());

    BpmnAwareTests.assertThat(firstMessageProcessInstance).isEnded();

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(USER_PROCESS_DEFINITION_KEY)
        .processInstanceId(userProcessInstanceId)
        .activityDefinitionId("user_form_4")
        .formKey("user-form-4")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(FormDataDto.builder()
            .data(new LinkedHashMap<>(Map.of("okMessageVariable", "okMessageVariableValue")))
            .build())
        .build());

    completeTask(CompleteActivityDto.builder()
        .processInstanceId(userProcessInstanceId)
        .activityDefinitionId("user_form_4")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("")
        .build());

    BpmnAwareTests.assertThat(userProcessInstance).isEnded();
  }
}
