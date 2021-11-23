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

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.lang.NonNull;

/**
 * Factory class that is used for creating {@link CompleterVariablesReadAccessor} and {@link
 * CompleterVariablesWriteAccessor}
 */
public interface CompleterVariablesAccessor {

  /**
   * Create {@link CompleterVariablesReadAccessor} that used {@link DelegateExecution} as a variable
   * storage
   *
   * @param delegateExecution variable storage
   * @return {@link CompleterVariablesReadAccessor} object
   */
  @NonNull
  CompleterVariablesReadAccessor from(@NonNull DelegateExecution delegateExecution);

  /**
   * Create {@link CompleterVariablesWriteAccessor} that used {@link DelegateExecution} as a
   * variable storage
   *
   * @param delegateExecution variable storage
   * @return {@link CompleterVariablesWriteAccessor} object
   */
  @NonNull
  CompleterVariablesWriteAccessor on(@NonNull DelegateExecution delegateExecution);
}
