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

package com.epam.digital.data.platform.dataaccessor.completer;

import com.epam.digital.data.platform.dataaccessor.VariableAccessor;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Base class that is used for <i>completer</i> variables accessing
 */
@RequiredArgsConstructor
public class BaseCompleterVariablesReadWriteAccessor implements CompleterVariablesWriteAccessor,
    CompleterVariablesReadAccessor {

  private static final String COMPLETER_VAR_TOKEN_FORMAT = "%s_completer_access_token";
  private static final String COMPLETER_VAR_NAME_FORMAT = "%s_completer";

  private final VariableAccessor variableAccessor;

  @Override
  public void setTaskCompleter(@NonNull String taskDefinitionKey, @Nullable String completerName) {
    var completerVariableName = getCompleterNameVariableName(taskDefinitionKey);
    variableAccessor.setVariable(completerVariableName, completerName);
  }

  @Override
  public void setTaskCompleterToken(@NonNull String taskDefinitionKey,
      @Nullable String completerToken) {
    var completerTokenVariableName = getCompleterTokenVariableName(taskDefinitionKey);
    variableAccessor.setVariableTransient(completerTokenVariableName, completerToken);
  }

  @Override
  @NonNull
  public Optional<String> getTaskCompleter(@NonNull String taskDefinitionKey) {
    var completerVariableName = getCompleterNameVariableName(taskDefinitionKey);
    return Optional.ofNullable(variableAccessor.getVariable(completerVariableName));
  }

  @Override
  @NonNull
  public Optional<String> getTaskCompleterToken(@NonNull String taskDefinitionKey) {
    var completerTokenVariableName = getCompleterTokenVariableName(taskDefinitionKey);
    return Optional.ofNullable(variableAccessor.getVariable(completerTokenVariableName));
  }

  private String getCompleterNameVariableName(String taskDefinitionKey) {
    return String.format(COMPLETER_VAR_NAME_FORMAT, taskDefinitionKey);
  }

  private String getCompleterTokenVariableName(String taskDefinitionKey) {
    return String.format(COMPLETER_VAR_TOKEN_FORMAT, taskDefinitionKey);
  }
}
