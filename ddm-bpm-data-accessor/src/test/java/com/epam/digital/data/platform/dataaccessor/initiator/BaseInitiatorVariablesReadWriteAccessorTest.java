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

package com.epam.digital.data.platform.dataaccessor.initiator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.VariableAccessor;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BaseInitiatorVariablesReadWriteAccessorTest {

  @InjectMocks
  private BaseInitiatorVariablesReadWriteAccessor baseInitiatorVariablesReadWriteAccessor;
  @Mock
  private VariableAccessor variableAccessor;
  @Mock
  private ExecutionEntity execution;
  @Mock
  private ProcessDefinitionEntity processDefinitionEntity;

  @Test
  void setInitiatorAccessToken() {
    var token = "token";

    baseInitiatorVariablesReadWriteAccessor.setInitiatorAccessToken(token);

    verify(variableAccessor).setVariableTransient(
        BaseInitiatorVariablesReadWriteAccessor.INITIATOR_TOKEN_VAR_NAME, token);
  }

  @Test
  void getInitiatorName() {
    var initiatorName = "initiatorName";
    var initiatorVariable = "initiator";

    when(execution.getProcessDefinition()).thenReturn(processDefinitionEntity);
    when(processDefinitionEntity.getProperty(
        BpmnParse.PROPERTYNAME_INITIATOR_VARIABLE_NAME)).thenReturn(initiatorVariable);
    when(variableAccessor.getVariable(initiatorVariable)).thenReturn(initiatorName);

    var result = baseInitiatorVariablesReadWriteAccessor.getInitiatorName();

    assertThat(result).isNotEmpty().get().isSameAs(initiatorName);
  }

  @Test
  void getInitiatorAccessToken() {
    var token = "token";
    when(variableAccessor.getVariable(
        BaseInitiatorVariablesReadWriteAccessor.INITIATOR_TOKEN_VAR_NAME)).thenReturn(token);

    var result = baseInitiatorVariablesReadWriteAccessor.getInitiatorAccessToken();

    assertThat(result).isNotEmpty().get().isSameAs(token);
  }
}
