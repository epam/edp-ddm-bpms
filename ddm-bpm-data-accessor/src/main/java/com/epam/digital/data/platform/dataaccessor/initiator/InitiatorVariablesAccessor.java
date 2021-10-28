package com.epam.digital.data.platform.dataaccessor.initiator;

import org.camunda.bpm.engine.delegate.DelegateExecution;

/**
 * Factory class that is used for creating {@link InitiatorVariablesReadAccessor} and {@link
 * InitiatorVariablesWriteAccessor}
 */
public interface InitiatorVariablesAccessor {

  /**
   * Create {@link InitiatorVariablesReadAccessor} that used {@link DelegateExecution} as a variable
   * storage
   *
   * @param delegateExecution variable storage
   * @return {@link InitiatorVariablesReadAccessor} object
   */
  InitiatorVariablesReadAccessor from(DelegateExecution delegateExecution);

  /**
   * Create {@link InitiatorVariablesWriteAccessor} that used {@link DelegateExecution} as a
   * variable storage
   *
   * @param delegateExecution variable storage
   * @return {@link InitiatorVariablesWriteAccessor} object
   */
  InitiatorVariablesWriteAccessor on(DelegateExecution delegateExecution);

}
