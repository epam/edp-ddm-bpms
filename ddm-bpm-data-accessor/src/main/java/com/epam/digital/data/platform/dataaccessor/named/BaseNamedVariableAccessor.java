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

package com.epam.digital.data.platform.dataaccessor.named;

import com.epam.digital.data.platform.dataaccessor.DelegateExecutionVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.lang.NonNull;

/**
 * Base implementation of {@link NamedVariableAccessor} that contains info about variable name and
 * transient flag. Creates {@link BaseNamedVariableReadAccessor} as read-accessor and {@link
 * BaseNamedVariableWriteAccessor} as write-accessor. Also uses {@link
 * DelegateExecutionVariableAccessor} as base variable accessor that is used in the read- and
 * write-accessors.
 *
 * @param <T> type of the variable
 */
@RequiredArgsConstructor
public class BaseNamedVariableAccessor<T> implements NamedVariableAccessor<T> {

  private final String variableName;
  private final boolean isTransient;

  private final VariableAccessorFactory variableAccessorFactory;

  @NonNull
  @Override
  public NamedVariableReadAccessor<T> from(DelegateExecution execution) {
    var baseVariableAccessor = variableAccessorFactory.from(execution);
    return new BaseNamedVariableReadAccessor<>(variableName, baseVariableAccessor);
  }

  @NonNull
  @Override
  public NamedVariableWriteAccessor<T> on(DelegateExecution execution) {
    var baseVariableAccessor = variableAccessorFactory.from(execution);
    return new BaseNamedVariableWriteAccessor<>(variableName, baseVariableAccessor, isTransient);
  }
}
