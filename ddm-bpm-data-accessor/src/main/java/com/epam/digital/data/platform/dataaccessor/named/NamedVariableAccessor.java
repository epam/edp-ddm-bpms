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
