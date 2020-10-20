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

import org.springframework.lang.NonNull;

/**
 * Main class that is used for creating variable accessors with predefined name, type and transient
 * flag. Used for creating variable API for an extension.
 * <p>
 * For example
 * <pre> FixedVariableAccessor&#60;String&#62; variable =
 * variableAccessorFactory.variableAccessor("variable", false);</pre> can be used for creating
 * accessor of non-transient variable with name {@code variable} and type {@code String}
 *
 * @see NamedVariableWriteAccessor the variable accessor itself
 */
public interface NamedVariableAccessorFactory {

  /**
   * Creates variable accessors with predefined name, type and transient flag
   *
   * @param name        variable name
   * @param isTransient variable transient flag
   * @param <T>         variable value type
   * @return the accessor
   */
  @NonNull
  <T> NamedVariableAccessor<T> variableAccessor(String name, boolean isTransient);
}
