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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;

public class SubProcessByMessageBpmnIT extends BaseBpmnIT {

  @Test
  @Deployment(resources = "bpmn/sub-process-by-message.bpmn")
  public void test() throws JsonProcessingException {
    var receiveMessageForEventSubProcessInstanceId = startProcessInstance(
        "receiveMessageForEventSubProcess", testUserToken);
    var receiveMessageForEventSubProcessInstance = processInstance(
        receiveMessageForEventSubProcessInstanceId);

    var sendMessageForEventSubProcessInstanceId = startProcessInstance(
        "sendMessageForEventSubProcess", testUserToken);
    var sendMessageForEventSubProcessInstance = BpmnAwareTests.historyService()
        .createHistoricProcessInstanceQuery()
        .processInstanceId(sendMessageForEventSubProcessInstanceId)
        .singleResult();

    Assertions.assertThat(sendMessageForEventSubProcessInstance)
        .isNotNull()
        .extracting(HistoricProcessInstance::getState)
        .isEqualTo("COMPLETED");

    BpmnAwareTests.assertThat(receiveMessageForEventSubProcessInstance)
        .isEnded()
        .variables()
        .containsEntry("global_variable", "variableValue")
        .containsEntry("correlationVariable", "correlationVariableValue");
  }
}
