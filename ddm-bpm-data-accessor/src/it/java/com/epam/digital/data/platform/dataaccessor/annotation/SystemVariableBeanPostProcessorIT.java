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

package com.epam.digital.data.platform.dataaccessor.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.config.DataAccessorTestConfiguration;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(DataAccessorTestConfiguration.class)
class SystemVariableBeanPostProcessorIT {

  private static final String VALID_VARIABLE_NAME = "validVariable";
  private static final String VALID_TRANSIENT_VARIABLE_NAME = "validTransientVariable";
  private static final String VARIABLE_VALUE = "value";

  @SystemVariable(name = "wrongFieldType")
  private String wrongFieldType;

  @SuppressWarnings("unused")
  private NamedVariableAccessor<String> noAnnotatedField;

  @SystemVariable(name = VALID_VARIABLE_NAME)
  private NamedVariableAccessor<String> validVariable;
  @SystemVariable(name = VALID_TRANSIENT_VARIABLE_NAME, isTransient = true)
  private NamedVariableAccessor<String> validTransientVariable;

  @Mock
  private ExecutionEntity delegateExecution;

  @Test
  void testFieldInstantiation() {
    assertThat(wrongFieldType).isNull();
    assertThat(noAnnotatedField).isNull();
    assertThat(validVariable).isNotNull();
    assertThat(validTransientVariable).isNotNull();
  }

  @Test
  void testVariableAccessing() {
    when(delegateExecution.getVariable(VALID_VARIABLE_NAME)).thenReturn(VARIABLE_VALUE);
    assertThat(validVariable.from(delegateExecution).get()).isEqualTo(VARIABLE_VALUE);

    clearInvocations(delegateExecution);
    when(delegateExecution.getVariable(VALID_TRANSIENT_VARIABLE_NAME)).thenReturn(VARIABLE_VALUE);
    assertThat(validTransientVariable.from(delegateExecution).get()).isEqualTo(VARIABLE_VALUE);

    clearInvocations(delegateExecution);
    validVariable.on(delegateExecution).set(VARIABLE_VALUE);
    verify(delegateExecution).setVariable(VALID_VARIABLE_NAME, VARIABLE_VALUE);

    clearInvocations(delegateExecution);
    validTransientVariable.on(delegateExecution).set(VARIABLE_VALUE);
    verify(delegateExecution).setVariableLocal(VALID_TRANSIENT_VARIABLE_NAME,
        Variables.untypedValue(VARIABLE_VALUE, true));

    clearInvocations(delegateExecution);
    validVariable.on(delegateExecution).setLocal(VARIABLE_VALUE);
    verify(delegateExecution).setVariableLocal(VALID_VARIABLE_NAME, VARIABLE_VALUE);

    clearInvocations(delegateExecution);
    validTransientVariable.on(delegateExecution).setLocal(VARIABLE_VALUE);
    verify(delegateExecution).setVariableLocal(VALID_TRANSIENT_VARIABLE_NAME,
        Variables.untypedValue(VARIABLE_VALUE, true));

    clearInvocations(delegateExecution);
    validTransientVariable.on(delegateExecution).remove();
    verify(delegateExecution).removeVariable(VALID_TRANSIENT_VARIABLE_NAME);
  }
}
