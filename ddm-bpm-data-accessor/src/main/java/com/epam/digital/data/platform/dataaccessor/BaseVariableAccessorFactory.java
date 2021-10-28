package com.epam.digital.data.platform.dataaccessor;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.lang.NonNull;

/**
 * Base implementation of {@link VariableAccessorFactory}
 */
public class BaseVariableAccessorFactory implements VariableAccessorFactory {

  /**
   * Creates {@link DelegateExecutionVariableAccessor} object
   *
   * @param delegateExecution variable storage
   * @return {@link DelegateExecutionVariableAccessor}
   */
  @NonNull
  public VariableAccessor from(@NonNull DelegateExecution delegateExecution) {
    return new DelegateExecutionVariableAccessor(delegateExecution);
  }
}
