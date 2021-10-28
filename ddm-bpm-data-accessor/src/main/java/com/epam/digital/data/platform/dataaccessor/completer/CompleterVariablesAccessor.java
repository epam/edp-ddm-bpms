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
