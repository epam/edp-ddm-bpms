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

import org.springframework.lang.Nullable;

/**
 * Class that is used for write accessing the variable with predefined name on variable storage
 *
 * @param <T> type of the variable
 */
public interface NamedVariableWriteAccessor<T> {

  /**
   * Set variable value to variable storage
   *
   * @param value variable value to set (nullable)
   */
  void set(@Nullable T value);

  /**
   * Set variable value to local scope of variable storage
   *
   * @param value variable value to set (nullable)
   */
  void setLocal(@Nullable T value);

  /**
   * Remove variable value from variable storage if exist
   */
  void remove();
}
