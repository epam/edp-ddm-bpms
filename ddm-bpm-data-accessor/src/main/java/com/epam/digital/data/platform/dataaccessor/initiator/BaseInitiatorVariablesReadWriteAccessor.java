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

import com.epam.digital.data.platform.dataaccessor.VariableAccessor;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Base class that is used for <i>initiator</i> variables accessing
 */
@RequiredArgsConstructor
public class BaseInitiatorVariablesReadWriteAccessor implements InitiatorVariablesReadAccessor,
    InitiatorVariablesWriteAccessor {

  public static final String INITIATOR_TOKEN_VAR_NAME = "initiator_access_token";

  private final VariableAccessor variableAccessor;
  private final ExecutionEntity execution;

  @NonNull
  @Override
  public Optional<String> getInitiatorName() {
    final var initiatorVarName = (String) execution.getProcessDefinition()
        .getProperty(BpmnParse.PROPERTYNAME_INITIATOR_VARIABLE_NAME);
    return Optional.ofNullable(variableAccessor.getVariable(initiatorVarName));
  }

  @NonNull
  @Override
  public Optional<String> getInitiatorAccessToken() {
    return Optional.ofNullable(variableAccessor.getVariable(INITIATOR_TOKEN_VAR_NAME));
  }

  @Override
  public void setInitiatorAccessToken(@Nullable String accessToken) {
    variableAccessor.setVariableTransient(INITIATOR_TOKEN_VAR_NAME, accessToken);
  }
}
