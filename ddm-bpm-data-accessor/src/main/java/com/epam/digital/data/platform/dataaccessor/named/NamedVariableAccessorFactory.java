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
