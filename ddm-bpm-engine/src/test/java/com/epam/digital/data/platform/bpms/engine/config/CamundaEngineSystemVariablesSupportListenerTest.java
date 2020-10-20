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

package com.epam.digital.data.platform.bpms.engine.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.VariableAccessor;
import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;

import org.assertj.core.util.Maps;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CamundaEngineSystemVariablesSupportListenerTest {

  @InjectMocks
  private CamundaEngineSystemVariablesSupportListener camundaSystemVariablesSupportListener;
  @Mock
  private CamundaProperties camundaProperties;
  @Mock
  private VariableAccessorFactory variableAccessorFactory;
  @Mock
  private ProcessDefinitionImpl processDefinition;

  @Captor
  private ArgumentCaptor<ExecutionListener> executionListenerArgumentCaptor;

  @Test
  void shouldAddListenerThatAddsCamundaSystemPropertiesToBpmn() throws Exception {
    when(camundaProperties.getSystemVariables()).thenReturn(Maps.newHashMap("var1", "value1"));

    var delegateExecution = mock(DelegateExecution.class);
    var variableAccessor = mock(VariableAccessor.class);
    when(variableAccessorFactory.from(delegateExecution)).thenReturn(variableAccessor);

    var activity = mock(ActivityImpl.class);
    camundaSystemVariablesSupportListener.parseStartEvent(null, processDefinition, activity);

    verify(activity).addBuiltInListener(eq(ExecutionListener.EVENTNAME_START),
        executionListenerArgumentCaptor.capture());
    var executionListener = executionListenerArgumentCaptor.getValue();
    assertThat(executionListener).isNotNull();
    executionListener.notify(delegateExecution);
    verify(variableAccessor).setVariable("var1", "value1");
  }
}
