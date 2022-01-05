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

package com.epam.digital.data.platform.dataaccessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelegateExecutionVariableAccessorTest {

  private static final String VARIABLE_NAME = "variable";
  private static final String VARIABLE_VALUE = "value";

  @InjectMocks
  private DelegateExecutionVariableAccessor delegateExecutionVariableAccessor;
  @Mock
  private ExecutionEntity delegateExecution;

  @Test
  void getVariable() {
    when(delegateExecution.getVariable(VARIABLE_NAME)).thenReturn(VARIABLE_VALUE);
    assertThat((String) delegateExecutionVariableAccessor.getVariable(VARIABLE_NAME))
        .isEqualTo(VARIABLE_VALUE);

    when(delegateExecution.getVariable(VARIABLE_NAME)).thenReturn(null);
    assertThat((String) delegateExecutionVariableAccessor.getVariable(VARIABLE_NAME)).isNull();
  }

  @Test
  void setVariable() {
    delegateExecutionVariableAccessor.setVariable(VARIABLE_NAME, VARIABLE_VALUE);

    verify(delegateExecution).setVariable(VARIABLE_NAME, VARIABLE_VALUE);
  }

  @Test
  void setVariableLocal() {
    delegateExecutionVariableAccessor.setVariableLocal(VARIABLE_NAME, VARIABLE_VALUE);

    verify(delegateExecution).setVariableLocal(VARIABLE_NAME, VARIABLE_VALUE);
  }

  @Test
  void setVariableTransient() {
    delegateExecutionVariableAccessor.setVariableTransient(VARIABLE_NAME, VARIABLE_VALUE);

    verify(delegateExecution).setVariableLocal(VARIABLE_NAME,
        Variables.untypedValue(VARIABLE_VALUE, true));
  }

  @Test
  void removeVariable() {
    delegateExecutionVariableAccessor.removeVariable(VARIABLE_NAME);

    verify(delegateExecution).removeVariable(VARIABLE_NAME);
  }
}
