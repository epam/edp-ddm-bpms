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

import java.util.Objects;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Class that is used for read accessing the variable with predefined name from variable storage
 *
 * @param <T> type of the variable
 */
public interface NamedVariableReadAccessor<T> {

  /**
   * Get nullable variable value from storage
   *
   * @return return variable value if variable is present or {@code null}
   */
  @Nullable
  T get();

  /**
   * Get name of variable
   *
   * @return variable name
   */
  String getName();

  /**
   * Get optional variable value from storage
   *
   * @return return optional variable value object
   */
  @NonNull
  default Optional<T> getOptional() {
    return Optional.ofNullable(get());
  }

  /**
   * Get variable value from storage or else default value
   *
   * @param defaultValue value that will be returned in case of {@code null} variable value
   * @return return variable value if the variable present and not null or else default
   */
  @NonNull
  default T getOrDefault(T defaultValue) {
    return Objects.requireNonNullElse(get(), defaultValue);
  }

  /**
   * Get variable value or else throw {@link IllegalArgumentException}
   *
   * @return variable value if the variable present and not null or else throw exception
   */
  @NonNull
  default T getOrThrow() {
    return this.getOptional().orElseThrow(
        () -> new IllegalArgumentException(String.format("Variable %s not found", getName())));
  }
}
