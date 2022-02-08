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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableReadAccessor;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
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
class SendMessageDelegateTest {

  @Mock
  private RuntimeService runtimeService;
  @Mock
  private NamedVariableAccessor<String> correlationProcessInstanceIdVariable;
  @Mock
  private NamedVariableAccessor<Map<String, Object>> correlationVariablesVariable;
  @Mock
  private NamedVariableAccessor<Map<String, Object>> dataVariable;
  @Mock
  private NamedVariableReadAccessor<String> correlationProcessInstanceIdVariableReadAccessor;
  @Mock
  private NamedVariableReadAccessor<Map<String, Object>> correlationVariablesVariableReadAccessor;
  @Mock
  private NamedVariableReadAccessor<Map<String, Object>> dataVariableReadAccessor;

  @Mock
  private DelegateExecution delegateExecution;
  @Mock
  private MessageCorrelationBuilder messageCorrelationBuilder;

  @InjectMocks
  private SendMessageDelegate sendMessageDelegate;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(sendMessageDelegate, "correlationProcessInstanceIdVariable",
        correlationProcessInstanceIdVariable);
    ReflectionTestUtils.setField(sendMessageDelegate, "correlationVariablesVariable",
        correlationVariablesVariable);
    ReflectionTestUtils.setField(sendMessageDelegate, "dataVariable",
        dataVariable);
    lenient().when(correlationProcessInstanceIdVariable.from(delegateExecution))
        .thenReturn(correlationProcessInstanceIdVariableReadAccessor);
    lenient().when(correlationVariablesVariable.from(delegateExecution))
        .thenReturn(correlationVariablesVariableReadAccessor);
    lenient().when(dataVariable.from(delegateExecution))
        .thenReturn(dataVariableReadAccessor);

    var modelInstanceId = UUID.randomUUID().toString();
    lenient().when(delegateExecution.getCurrentActivityId()).thenReturn(modelInstanceId);

    var bpmnModel = mock(BpmnModelInstance.class);
    lenient().when(delegateExecution.getBpmnModelInstance()).thenReturn(bpmnModel);
    var currentActivity = mock(ModelElementInstance.class);
    lenient().when(bpmnModel.getModelElementById(modelInstanceId)).thenReturn(currentActivity);

    var messageEventDefinition = mock(MessageEventDefinition.class);
    lenient().when(currentActivity.getUniqueChildElementByType(MessageEventDefinition.class))
        .thenReturn(messageEventDefinition);

    var messageRefId = UUID.randomUUID().toString();
    lenient().when(messageEventDefinition.getAttributeValue("messageRef")).thenReturn(messageRefId);

    var messageModelElement = mock(ModelElementInstance.class);
    lenient().when(bpmnModel.getModelElementById(messageRefId)).thenReturn(messageModelElement);
    var messageName = "test_message_name";
    lenient().when(messageModelElement.getAttributeValue("name")).thenReturn(messageName);

    lenient().when(runtimeService.createMessageCorrelation(messageName))
        .thenReturn(messageCorrelationBuilder);
  }

  @Test
  void getName() {
    assertThat(sendMessageDelegate.getDelegateName()).isEqualTo(SendMessageDelegate.DELEGATE_NAME);
  }

  @Test
  void execute_noCorrelationIdentifiers() {
    when(correlationProcessInstanceIdVariableReadAccessor.get()).thenReturn(null);
    when(correlationVariablesVariableReadAccessor.getOrDefault(Map.of())).thenReturn(Map.of());

    var ex = assertThrows(IllegalArgumentException.class,
        () -> sendMessageDelegate.execute(delegateExecution));

    assertThat(ex.getMessage()).isEqualTo(
        "Can't correlate message to process due to missing correlation identifiers");
  }

  @Test
  void execute_correlationProcessInstanceId() throws Exception {
    var correlationProcessInstanceId = "correlationProcessInstanceId";
    Map<String, Object> data = Map.of("stringKey", "value");
    when(correlationProcessInstanceIdVariableReadAccessor.get())
        .thenReturn(correlationProcessInstanceId);
    when(correlationVariablesVariableReadAccessor.getOrDefault(Map.of())).thenReturn(Map.of());
    when(dataVariableReadAccessor.getOrDefault(Map.of())).thenReturn(data);

    when(messageCorrelationBuilder.setVariables(data)).thenReturn(messageCorrelationBuilder);
    when(messageCorrelationBuilder.processInstanceVariablesEqual(Map.of()))
        .thenReturn(messageCorrelationBuilder);
    when(messageCorrelationBuilder.processInstanceId(correlationProcessInstanceId))
        .thenReturn(messageCorrelationBuilder);

    sendMessageDelegate.execute(delegateExecution);

    verify(messageCorrelationBuilder).setVariables(data);
    verify(messageCorrelationBuilder).processInstanceVariablesEqual(Map.of());
    verify(messageCorrelationBuilder).processInstanceId(correlationProcessInstanceId);
    verify(messageCorrelationBuilder).correlateWithResult();
  }

  @Test
  void execute_correlationVariables() throws Exception {
    Map<String, Object> correlationVariables = Map.of("correlationVariable", "correlationValue");
    Map<String, Object> data = Map.of("stringKey", "value");
    when(correlationProcessInstanceIdVariableReadAccessor.get()).thenReturn("");
    when(correlationVariablesVariableReadAccessor.getOrDefault(Map.of()))
        .thenReturn(correlationVariables);
    when(dataVariableReadAccessor.getOrDefault(Map.of())).thenReturn(data);

    when(messageCorrelationBuilder.setVariables(data)).thenReturn(messageCorrelationBuilder);
    when(messageCorrelationBuilder.processInstanceVariablesEqual(correlationVariables))
        .thenReturn(messageCorrelationBuilder);

    sendMessageDelegate.execute(delegateExecution);

    verify(messageCorrelationBuilder).setVariables(data);
    verify(messageCorrelationBuilder).processInstanceVariablesEqual(correlationVariables);
    verify(messageCorrelationBuilder, never()).processInstanceId(any());
    verify(messageCorrelationBuilder, never()).correlateWithResult();
    verify(messageCorrelationBuilder).correlateAllWithResult();
  }
}
