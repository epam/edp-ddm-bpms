package com.epam.digital.data.platform.dataaccessor;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.lang.NonNull;

/**
 * Class that is used for creating {@link VariableAccessor}
 */
public interface VariableAccessorFactory {

  /**
   * Returns {@link VariableAccessor} object that is used {@link DelegateExecution} as a
   * variable storage
   *
   * @param delegateExecution variable storage
   * @return variable accessor
   */
  @NonNull
  VariableAccessor from(@NonNull DelegateExecution delegateExecution);
}
