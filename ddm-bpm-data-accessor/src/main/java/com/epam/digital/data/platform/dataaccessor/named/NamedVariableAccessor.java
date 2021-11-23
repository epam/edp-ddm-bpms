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

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.lang.NonNull;

/**
 * Class that is used for creating {@link NamedVariableReadAccessor} and {@link
 * NamedVariableWriteAccessor} for variable with predefined name on {@link DelegateExecution} as a
 * variable storage
 * <p>
 * Example of usage ({@code variable} - instance of this class, {@code execution} - instance of
 * {@link DelegateExecution} class):
 * <pre>
 * variable.from(execution).get(); // for getting variable value
 * variable.on(execution).set("value"); // for setting variable value
 * </pre>
 *
 * @param <T> type of the variable
 * @see NamedVariableReadAccessor
 * @see NamedVariableWriteAccessor
 */
public interface NamedVariableAccessor<T> {

  /**
   * Create read variable accessor from {@link DelegateExecution} as a variable storage
   *
   * @param execution - variable storage
   * @return the accessor that is used for read only access to variable
   */
  @NonNull
  NamedVariableReadAccessor<T> from(DelegateExecution execution);

  /**
   * Create write variable accessor on {@link DelegateExecution} as a variable storage
   *
   * @param execution - variable storage
   * @return the accessor that is used for write only access to variable
   */
  @NonNull
  NamedVariableWriteAccessor<T> on(DelegateExecution execution);

}
