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

package com.epam.digital.data.platform.bpms.storage;

import com.epam.digital.data.platform.dataaccessor.sysvar.StartMessagePayloadStorageKeyVariable;
import com.epam.digital.data.platform.storage.message.dto.MessagePayloadDto;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.jupiter.api.Test;

class MessagePayloadCleanerEndEventListenerIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/message_payload_cleaner_listener.bpmn")
  void shouldDeleteStartFormDataTest() {
    var processDefinition = "processDefinitionId";
    var randomUUID = "randomUUID";
    var messagePayload = MessagePayloadDto.builder()
        .data(Map.of("name", "TestName"))
        .build();
    var cephKey = messagePayloadStorageService.putStartMessagePayload(processDefinition, randomUUID,
        messagePayload);
    Map<String, Object> vars = Map.of(
        StartMessagePayloadStorageKeyVariable.START_MESSAGE_PAYLOAD_STORAGE_KEY_VARIABLE_NAME, cephKey);

    var processInstance = BpmnAwareTests.runtimeService()
        .startProcessInstanceByMessage("messagePayloadCleanerListenerMessage", vars);

    Assertions.assertThat(cephService.getStorage()).hasSize(1);

    BpmnAwareTests.complete(BpmnAwareTests.task(processInstance));

    BpmnAwareTests.assertThat(processInstance).isEnded();
    Assertions.assertThat(cephService.getStorage()).isEmpty();
  }
}
