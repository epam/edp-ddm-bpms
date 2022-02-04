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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.extension.exception.CamundaMessageException;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableReadAccessor;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableWriteAccessor;
import com.epam.digital.data.platform.dataaccessor.sysvar.CallerProcessInstanceIdVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartMessagePayloadStorageKeyVariable;
import com.epam.digital.data.platform.storage.message.dto.MessagePayloadDto;
import com.epam.digital.data.platform.storage.message.service.MessagePayloadStorageService;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StartProcessByMessageDelegateTest {

  private static final String MESSAGE_NAME = "test_message_name";
  private static final String CURRENT_PROCESS_INSTANCE_ID = UUID.randomUUID().toString();
  private static final String TARGET_PROCESS_DEFINITION_KEY = "targetProcessDefinition";
  private static final String CALLED_PROCESS_INSTANCE_ID = UUID.randomUUID().toString();

  @Mock
  private ProcessEngine processEngine;
  @Mock
  private RepositoryService repositoryService;
  @Mock
  private RuntimeService runtimeService;
  @Mock
  private MessagePayloadStorageService messagePayloadStorageService;
  @Mock
  private NamedVariableAccessor<Map<String, Object>> messagePayloadVariable;
  @Mock
  private NamedVariableReadAccessor<Map<String, Object>> messagePayloadVariableReadAccessor;
  @Mock
  private NamedVariableAccessor<String> calledProcessInstanceIdVariable;
  @Mock
  private NamedVariableWriteAccessor<String> calledProcessInstanceIdVariableWriteAccessor;

  @Mock
  private ProcessDefinitionQuery processDefinitionQuery;
  @Mock
  private MessageCorrelationBuilder messageCorrelationBuilder;
  @Mock
  private ProcessInstance processInstance;

  @Mock
  private DelegateExecution delegateExecution;
  @Mock
  private ModelElementInstance currentActivity;

  @InjectMocks
  private StartProcessByMessageDelegate startProcessByMessageDelegate;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(startProcessByMessageDelegate, "messagePayloadVariable",
        messagePayloadVariable);
    ReflectionTestUtils.setField(startProcessByMessageDelegate, "calledProcessInstanceIdVariable",
        calledProcessInstanceIdVariable);
    lenient().when(processEngine.getRepositoryService()).thenReturn(repositoryService);
    lenient().when(processEngine.getRuntimeService()).thenReturn(runtimeService);
    lenient().when(messagePayloadVariable.from(delegateExecution))
        .thenReturn(messagePayloadVariableReadAccessor);
    lenient().when(calledProcessInstanceIdVariable.on(delegateExecution))
        .thenReturn(calledProcessInstanceIdVariableWriteAccessor);

    lenient().when(delegateExecution.getProcessInstanceId())
        .thenReturn(CURRENT_PROCESS_INSTANCE_ID);
    var modelInstanceId = UUID.randomUUID().toString();
    lenient().when(delegateExecution.getCurrentActivityId()).thenReturn(modelInstanceId);

    var bpmnModel = mock(BpmnModelInstance.class);
    lenient().when(delegateExecution.getBpmnModelInstance()).thenReturn(bpmnModel);
    lenient().when(bpmnModel.getModelElementById(modelInstanceId)).thenReturn(currentActivity);

    var messageEventDefinition = mock(MessageEventDefinition.class);
    lenient().when(currentActivity.getUniqueChildElementByType(MessageEventDefinition.class))
        .thenReturn(messageEventDefinition);

    var messageRefId = UUID.randomUUID().toString();
    lenient().when(messageEventDefinition.getAttributeValue("messageRef")).thenReturn(messageRefId);

    var messageModelElement = mock(ModelElementInstance.class);
    lenient().when(bpmnModel.getModelElementById(messageRefId)).thenReturn(messageModelElement);
    lenient().when(messageModelElement.getAttributeValue("name")).thenReturn(MESSAGE_NAME);

    var processDefinition = mock(ProcessDefinition.class);
    lenient().when(repositoryService.createProcessDefinitionQuery())
        .thenReturn(processDefinitionQuery);
    lenient().when(processDefinitionQuery.messageEventSubscriptionName(MESSAGE_NAME))
        .thenReturn(processDefinitionQuery);
    lenient().when(processDefinitionQuery.singleResult()).thenReturn(processDefinition);
    lenient().when(processDefinition.getKey()).thenReturn(TARGET_PROCESS_DEFINITION_KEY);

    lenient().when(runtimeService.createMessageCorrelation(MESSAGE_NAME))
        .thenReturn(messageCorrelationBuilder);
    lenient().when(messageCorrelationBuilder.setVariables(any()))
        .thenReturn(messageCorrelationBuilder);
    lenient().when(messageCorrelationBuilder.correlateStartMessage()).thenReturn(processInstance);
    lenient().when(processInstance.getProcessInstanceId()).thenReturn(CALLED_PROCESS_INSTANCE_ID);
  }

  @Test
  void getName() {
    assertThat(startProcessByMessageDelegate.getDelegateName())
        .isEqualTo(StartProcessByMessageDelegate.DELEGATE_NAME);
  }

  @Test
  void execute_currentActivityNotMessageThrowEvent() {
    when(currentActivity.getUniqueChildElementByType(MessageEventDefinition.class))
        .thenReturn(null);

    var ex = assertThrows(CamundaMessageException.class,
        () -> startProcessByMessageDelegate.execute(delegateExecution));
    assertThat(ex.getMessage()).isEqualTo(
        "Delegate startProcessByMessageDelegate has to be used only in message events");
  }

  @Test
  void execute_noMessagePayload() throws Exception {
    when(messagePayloadVariableReadAccessor.getOptional()).thenReturn(Optional.empty());

    startProcessByMessageDelegate.execute(delegateExecution);

    verify(messageCorrelationBuilder).setVariables(
        Map.of(CallerProcessInstanceIdVariable.CALLER_PROCESS_INSTANCE_ID_VARIABLE_NAME,
            CURRENT_PROCESS_INSTANCE_ID));
    verify(calledProcessInstanceIdVariableWriteAccessor).set(CALLED_PROCESS_INSTANCE_ID);
  }

  @Test
  void execute_noProcessDefinitionExists() {
    when(messagePayloadVariableReadAccessor.getOptional())
        .thenReturn(Optional.of(Map.of()));
    when(processDefinitionQuery.singleResult()).thenReturn(null);

    var ex = assertThrows(CamundaMessageException.class,
        () -> startProcessByMessageDelegate.execute(delegateExecution));
    assertThat(ex.getMessage()).isEqualTo(
        "No process definition correlated to message test_message_name found");
  }

  @Test
  void execute_messagePayloadExists() throws Exception {
    Map<String, Object> data = Map.of("variable", "value");
    when(messagePayloadVariableReadAccessor.getOptional()).thenReturn(Optional.of(data));

    var storageKey = "testStorageKey";
    when(messagePayloadStorageService.putStartMessagePayload(eq(TARGET_PROCESS_DEFINITION_KEY),
        any(), eq(MessagePayloadDto.builder().data(data).build())))
        .thenReturn(storageKey);

    startProcessByMessageDelegate.execute(delegateExecution);

    verify(messageCorrelationBuilder).setVariables(
        Map.of(CallerProcessInstanceIdVariable.CALLER_PROCESS_INSTANCE_ID_VARIABLE_NAME,
            CURRENT_PROCESS_INSTANCE_ID,
            StartMessagePayloadStorageKeyVariable.START_MESSAGE_PAYLOAD_STORAGE_KEY_VARIABLE_NAME,
            storageKey));
    verify(calledProcessInstanceIdVariableWriteAccessor).set(CALLED_PROCESS_INSTANCE_ID);
  }
}
