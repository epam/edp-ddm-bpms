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

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Class that is used for base accessing variables from variable storage
 */
public interface VariableAccessor {

  /**
   * Returns nullable variable value from variable storage
   *
   * @param name name of the variable
   * @param <T>  type of the variable
   * @return variable value or {@code null} if variable doesn't exist
   * @throws ClassCastException in case if variable in variable storage has different type than
   *                            expected
   */
  @Nullable
  <T> T getVariable(@NonNull String name);

  /**
   * Set new or override existed variable in variable storage
   *
   * @param name  name of the variable
   * @param value value of the variable
   * @param <T>   type of the variable
   */
  <T> void setVariable(@NonNull String name, @Nullable T value);

  /**
   * Set new or override existed variable in local scope of variable storage
   *
   * @param name  name of the variable
   * @param value value of the variable
   * @param <T>   type of the variable
   */
  <T> void setVariableLocal(@NonNull String name, @Nullable T value);

  /**
   * Set new or override existed transient variable in variable storage
   *
   * @param name  name of the variable
   * @param value value of the variable
   * @param <T>   type of the variable
   */
  <T> void setVariableTransient(@NonNull String name, @Nullable T value);

  /**
   * Remove variable from variable storage
   *
   * @param name variable name
   */
  void removeVariable(@NonNull String name);
}
