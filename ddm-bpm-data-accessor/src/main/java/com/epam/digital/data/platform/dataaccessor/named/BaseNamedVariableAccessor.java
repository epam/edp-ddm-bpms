package com.epam.digital.data.platform.dataaccessor.named;

import com.epam.digital.data.platform.dataaccessor.DelegateExecutionVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.lang.NonNull;

/**
 * Base implementation of {@link NamedVariableAccessor} that contains info about variable name and
 * transient flag. Creates {@link BaseNamedVariableReadAccessor} as read-accessor and {@link
 * BaseNamedVariableWriteAccessor} as write-accessor. Also uses {@link
 * DelegateExecutionVariableAccessor} as base variable accessor that is used in the read- and
 * write-accessors.
 *
 * @param <T> type of the variable
 */
@RequiredArgsConstructor
public class BaseNamedVariableAccessor<T> implements NamedVariableAccessor<T> {

  private final String variableName;
  private final boolean isTransient;

  private final VariableAccessorFactory variableAccessorFactory;

  @NonNull
  @Override
  public NamedVariableReadAccessor<T> from(DelegateExecution execution) {
    var baseVariableAccessor = variableAccessorFactory.from(execution);
    return new BaseNamedVariableReadAccessor<>(variableName, baseVariableAccessor);
  }

  @NonNull
  @Override
  public NamedVariableWriteAccessor<T> on(DelegateExecution execution) {
    var baseVariableAccessor = variableAccessorFactory.from(execution);
    return new BaseNamedVariableWriteAccessor<>(variableName, baseVariableAccessor, isTransient);
  }
}
