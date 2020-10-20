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

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Implementation of {@link VariableAccessor} that is used {@link DelegateExecution} as a storage
 * variable
 */
@RequiredArgsConstructor
public class DelegateExecutionVariableAccessor implements VariableAccessor {

  private final DelegateExecution execution;

  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public <T> T getVariable(@NonNull String name) {
    var variable = execution.getVariable(name);
    return Objects.isNull(variable) ? null : (T) variable;
  }

  @Override
  public void setVariable(@NonNull String name, @Nullable Object value) {
    execution.setVariable(name, value);
  }

  @Override
  public void setVariableLocal(@NonNull String name, @Nullable Object value) {
    execution.setVariableLocal(name, value);
  }

  @Override
  public void setVariableTransient(@NonNull String name, @Nullable Object value) {
    execution.setVariableLocal(name, Variables.untypedValue(value, true));
  }

  @Override
  public void removeVariable(@NonNull String name) {
    execution.removeVariable(name);
  }
}
