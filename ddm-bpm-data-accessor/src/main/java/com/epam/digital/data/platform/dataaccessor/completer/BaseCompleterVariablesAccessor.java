package com.epam.digital.data.platform.dataaccessor.completer;

import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.lang.NonNull;

/**
 * Base implementation of {@link CompleterVariablesAccessor} that creates {@link
 * BaseCompleterVariablesReadWriteAccessor} as implementation of {@link
 * CompleterVariablesReadAccessor} and {@link CompleterVariablesWriteAccessor}
 */
@RequiredArgsConstructor
public class BaseCompleterVariablesAccessor implements CompleterVariablesAccessor {

  private final VariableAccessorFactory variableAccessorFactory;

  @Override
  @NonNull
  public CompleterVariablesReadAccessor from(@NonNull DelegateExecution delegateExecution) {
    return accessor(delegateExecution);
  }

  @Override
  @NonNull
  public CompleterVariablesWriteAccessor on(@NonNull DelegateExecution delegateExecution) {
    return accessor(delegateExecution);
  }

  private BaseCompleterVariablesReadWriteAccessor accessor(DelegateExecution delegateExecution) {
    var variableAccessor = variableAccessorFactory.from(delegateExecution);
    return new BaseCompleterVariablesReadWriteAccessor(variableAccessor);
  }
}
