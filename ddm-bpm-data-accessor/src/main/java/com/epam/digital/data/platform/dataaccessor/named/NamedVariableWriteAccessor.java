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
