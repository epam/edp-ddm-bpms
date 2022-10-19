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

package com.epam.digital.data.platform.bpms.extension.it;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.dataaccessor.sysvar.CallerProcessInstanceIdVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartMessagePayloadStorageKeyVariable;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class StartProcessByMessageDelegateIT extends BaseIT {

  @Test
  @SneakyThrows
  @Deployment(resources = "bpmn/delegate/startProcessByMessageDelegate.bpmn")
  void startBpByMessage() throws JSONException {
    var sendMessage = runtimeService
        .startProcessInstanceByKey("sendMessage");

    assertThat(sendMessage).isEnded();

    var processInstances = historyService.createHistoricProcessInstanceQuery()
        .processDefinitionKey("receiveMessage").list();
    Assertions.assertThat(processInstances).hasSize(1);

    var processCaller = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstances.get(0).getId())
        .variableName(CallerProcessInstanceIdVariable.CALLER_PROCESS_INSTANCE_ID_VARIABLE_NAME)
        .singleResult();

    Assertions.assertThat(processCaller)
        .hasFieldOrPropertyWithValue("value", sendMessage.getId());

    var messagePayload = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstances.get(0).getId())
        .variableName(
            StartMessagePayloadStorageKeyVariable.START_MESSAGE_PAYLOAD_STORAGE_KEY_VARIABLE_NAME)
        .singleResult();

    Assertions.assertThat(messagePayload)
        .hasFieldOrProperty("value")
        .extracting(HistoricVariableInstance::getValue)
        .asString().startsWith("process-definition/receiveMessage/start-message/");

    Assertions.assertThat(messagePayloadStorageService.getMessagePayload(
        (String) messagePayload.getValue())).isPresent();

    var actual = objectMapper.writeValueAsString(
        messagePayloadStorageService.getMessagePayload((String) messagePayload.getValue()).get());
    JSONAssert.assertEquals("{\"data\":{\"payloadPart\":\"payloadPartValue\"}}",
        actual, true);
  }
}
