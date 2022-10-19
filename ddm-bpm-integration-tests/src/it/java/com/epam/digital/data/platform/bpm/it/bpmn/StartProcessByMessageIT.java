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
import static com.epam.digital.data.platform.bpm.it.util.TestUtils.getContent;

import com.epam.digital.data.platform.bpm.it.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpm.it.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpm.it.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.dataaccessor.sysvar.CallerProcessInstanceIdVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartMessagePayloadStorageKeyVariable;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class StartProcessByMessageIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_KEY = "processThatStartsAnotherProcessByMessage";
  private static final String TARGET_PROCESS_DEFINITION_KEY = "processThatIsStartedByAnotherProcessByMessage";

  @Test
  @Deployment(resources = "bpmn/start-process-by-message.bpmn")
  public void test() throws JsonProcessingException, JSONException {
    var processInstanceId = startProcessInstance(PROCESS_DEFINITION_KEY, testUserToken);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("user_form_1")
        .formKey("user-form-1")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName))
        .build());

    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("user_form_1")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/start-process-by-message/user_form_1.json")
        .build());

    BpmnAwareTests.assertThat(processInstance).isEnded();

    var processInstances = BpmnAwareTests.runtimeService().createProcessInstanceQuery()
        .processDefinitionKey(TARGET_PROCESS_DEFINITION_KEY).list();

    Assertions.assertThat(processInstances).hasSize(1);
    var newProcessInstance = processInstances.get(0);
    var newProcessInstanceId = newProcessInstance.getId();

    var messagePayloadStorageKey = BpmnAwareTests.runtimeService()
        .createVariableInstanceQuery()
        .processInstanceIdIn(newProcessInstanceId)
        .variableName(
            StartMessagePayloadStorageKeyVariable.START_MESSAGE_PAYLOAD_STORAGE_KEY_VARIABLE_NAME)
        .singleResult()
        .getValue().toString();

    JSONAssert.assertEquals(objectMapper.writeValueAsString(
        messagePayloadStorageService.getMessagePayload(messagePayloadStorageKey).get()),
        getContent("/json/start-process-by-message/expectedMessagePayload.json"),
        true);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(TARGET_PROCESS_DEFINITION_KEY)
        .processInstanceId(newProcessInstanceId)
        .activityDefinitionId("user_form_2")
        .formKey("user-form-2")
        .candidateUsers(List.of("second_enterprise_user"))
        .expectedVariables(Map.of(
            StartMessagePayloadStorageKeyVariable.START_MESSAGE_PAYLOAD_STORAGE_KEY_VARIABLE_NAME,
            messagePayloadStorageKey,
            CallerProcessInstanceIdVariable.CALLER_PROCESS_INSTANCE_ID_VARIABLE_NAME,
            processInstanceId))
        .expectedFormDataPrePopulation(FormDataDto.builder()
            .data(new LinkedHashMap<>(Map.of(
                "variable1", "variableValue1",
                "variable2", "variableValue2",
                "callerProcess", processInstanceId)))
            .build())
        .build());
  }
}
