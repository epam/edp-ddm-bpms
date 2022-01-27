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

package com.epam.digital.data.platform.bpms.engine.config.businesskey;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import joptsimple.internal.Strings;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.ActivityImpl;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessKeyExecutionListenerTest {

  @InjectMocks
  private BusinessKeyExecutionListener listener;
  @Mock
  private ExpressionManager expressionManager;

  @Mock
  private DelegateExecution delegateExecution;
  @Mock
  private CamundaProperties camundaProperties;
  @Mock
  private CamundaProperty businessKeyExpressionCamundaProperty;
  @Mock
  private Expression expression;

  @BeforeEach
  void setUp() {
    var bpmnModelInstance = mock(BpmnModelInstance.class);
    lenient().when(delegateExecution.getBpmnModelInstance()).thenReturn(bpmnModelInstance);

    lenient().when(delegateExecution.getCurrentActivityId()).thenReturn("currentActivityId");

    var currentActivity = mock(ActivityImpl.class);
    lenient().when(bpmnModelInstance.getModelElementById("currentActivityId"))
        .thenReturn(currentActivity);
    var extensionElements = mock(ExtensionElements.class);
    lenient().when(currentActivity.getChildElementsByType(ExtensionElements.class))
        .thenReturn(List.of(extensionElements));
    lenient().when(extensionElements.getChildElementsByType(CamundaProperties.class))
        .thenReturn(List.of(camundaProperties));

    lenient().when(camundaProperties.getCamundaProperties())
        .thenReturn(List.of(businessKeyExpressionCamundaProperty));
    lenient().when(businessKeyExpressionCamundaProperty.getCamundaName())
        .thenReturn("businessKeyExpression");
    lenient().when(businessKeyExpressionCamundaProperty.getCamundaValue())
        .thenReturn("expressionMock");
    lenient().when(expressionManager.createExpression("expressionMock")).thenReturn(expression);
  }

  @Test
  void notify_severalExtensionAttributes() {
    var otherCamundaProperty = mock(CamundaProperty.class);
    when(otherCamundaProperty.getCamundaName()).thenReturn("otherName");
    var severalCamundaProperties = List.of(otherCamundaProperty,
        businessKeyExpressionCamundaProperty, businessKeyExpressionCamundaProperty);

    when(camundaProperties.getCamundaProperties()).thenReturn(severalCamundaProperties);

    listener.notify(delegateExecution);

    verify(delegateExecution, never()).setProcessBusinessKey(any());
  }

  @Test
  void notify_expressionResolvingException() {
    when(expression.getValue(delegateExecution, delegateExecution))
        .thenThrow(new RuntimeException());

    listener.notify(delegateExecution);

    verify(delegateExecution, never()).setProcessBusinessKey(any());
  }

  @Test
  void notify_expressionResolvingResultIsTooLong() {
    when(expression.getValue(delegateExecution, delegateExecution))
        .thenReturn(Strings.repeat(' ', 256));

    listener.notify(delegateExecution);

    verify(delegateExecution, never()).setProcessBusinessKey(any());
  }

  @Test
  void notify_happyPath() {
    var businessKey = "businessKey";
    when(expression.getValue(delegateExecution, delegateExecution)).thenReturn(businessKey);

    listener.notify(delegateExecution);

    verify(delegateExecution).setProcessBusinessKey(businessKey);
  }
}
