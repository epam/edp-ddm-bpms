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

package com.epam.digital.data.platform.bpms.extension.delegate.message;

import com.epam.digital.data.platform.bpms.extension.exception.CamundaMessageException;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.sysvar.CallerProcessInstanceIdVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartMessagePayloadStorageKeyVariable;
import com.epam.digital.data.platform.storage.message.dto.MessagePayloadDto;
import com.epam.digital.data.platform.storage.message.service.MessagePayloadStorageService;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used send start process
 * instance message
 */
@Component(StartProcessByMessageDelegate.DELEGATE_NAME)
@RequiredArgsConstructor
public class StartProcessByMessageDelegate extends AbstractMessageDelegate {

  public static final String DELEGATE_NAME = "startProcessByMessageDelegate";

  private final ProcessEngine processEngine;
  private final MessagePayloadStorageService messagePayloadStorageService;

  @SystemVariable(name = "messagePayload") // in
  private NamedVariableAccessor<Map<String, Object>> messagePayloadVariable;

  @SystemVariable(name = "calledProcessInstanceId", isTransient = true) // out
  private NamedVariableAccessor<String> calledProcessInstanceIdVariable;

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Override
  protected void executeInternal(@NonNull DelegateExecution execution) {
    var correlationMessage = getMessageName(execution);
    var messagePayloadStorageKey = saveMessagePayloadToStorage(correlationMessage, execution);

    Map<String, Object> messageData = Maps.newHashMap();
    messageData.put(CallerProcessInstanceIdVariable.CALLER_PROCESS_INSTANCE_ID_VARIABLE_NAME,
        execution.getProcessInstanceId());
    messagePayloadStorageKey.ifPresent(storageKey -> messageData.put(
        StartMessagePayloadStorageKeyVariable.START_MESSAGE_PAYLOAD_STORAGE_KEY_VARIABLE_NAME,
        storageKey));

    var processInstance = correlateStartMessage(correlationMessage, messageData);
    var calledProcessInstanceId = processInstance.getProcessInstanceId();

    calledProcessInstanceIdVariable.on(execution).set(calledProcessInstanceId);
  }

  private Optional<String> saveMessagePayloadToStorage(
      @NonNull String correlationMessage,
      @NonNull DelegateExecution execution) {
    var messagePayload = messagePayloadVariable.from(execution).getOptional();
    if (messagePayload.isEmpty()) {
      return Optional.empty();
    }

    var targetProcessDefinitionKey = getMessageCorrelationProcessDefinitionKey(correlationMessage);
    var uuid = UUID.randomUUID().toString();

    var messagePayloadDto = MessagePayloadDto.builder().data(messagePayload.get()).build();
    var storageKey = messagePayloadStorageService.putStartMessagePayload(targetProcessDefinitionKey,
        uuid, messagePayloadDto);
    return Optional.of(storageKey);
  }

  private String getMessageCorrelationProcessDefinitionKey(
      @NonNull String messageName) {
    var repositoryService = processEngine.getRepositoryService();

    var processDefinition = repositoryService.createProcessDefinitionQuery()
        .messageEventSubscriptionName(messageName)
        .singleResult();
    if (Objects.isNull(processDefinition)) {
      throw new CamundaMessageException(
          String.format("No process definition correlated to message %s found", messageName));
    }
    return processDefinition.getKey();
  }

  private ProcessInstance correlateStartMessage(
      @NonNull String correlationMessage,
      @NonNull Map<String, Object> data) {
    var runtimeService = processEngine.getRuntimeService();

    return runtimeService
        .createMessageCorrelation(correlationMessage)
        .setVariables(data)
        .correlateStartMessage();
  }
}
