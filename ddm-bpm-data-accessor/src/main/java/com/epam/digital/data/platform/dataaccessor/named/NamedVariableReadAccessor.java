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
}
