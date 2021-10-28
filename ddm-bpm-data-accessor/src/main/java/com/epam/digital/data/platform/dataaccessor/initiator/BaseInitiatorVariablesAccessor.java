package com.epam.digital.data.platform.dataaccessor.initiator;

import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * Base implementation of {@link InitiatorVariablesAccessor} that creates {@link
 * BaseInitiatorVariablesReadWriteAccessor} as implementation of {@link
 * InitiatorVariablesReadAccessor} and {@link InitiatorVariablesWriteAccessor}
 */
@RequiredArgsConstructor
public class BaseInitiatorVariablesAccessor implements InitiatorVariablesAccessor {

  private final VariableAccessorFactory variableAccessorFactory;

  @Override
  public InitiatorVariablesReadAccessor from(DelegateExecution delegateExecution) {
    return accessor(delegateExecution);
  }

  @Override
  public InitiatorVariablesWriteAccessor on(DelegateExecution delegateExecution) {
    return accessor(delegateExecution);
  }

  private BaseInitiatorVariablesReadWriteAccessor accessor(DelegateExecution execution) {
    var variableAccessor = variableAccessorFactory.from(execution);
    return new BaseInitiatorVariablesReadWriteAccessor(variableAccessor,
        (ExecutionEntity) execution);
  }
}
