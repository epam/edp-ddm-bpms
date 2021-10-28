package com.epam.digital.data.platform.dataaccessor;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Implementation of {@link VariableAccessor} that is used {@link DelegateExecution} as a storage
 * variable
 */
@RequiredArgsConstructor
public class DelegateExecutionVariableAccessor implements VariableAccessor {

  private final DelegateExecution execution;

  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public <T> T getVariable(@NonNull String name) {
    var variable = execution.getVariable(name);
    return Objects.isNull(variable) ? null : (T) variable;
  }

  @Override
  public void setVariable(@NonNull String name, @Nullable Object value) {
    execution.setVariable(name, value);
  }

  @Override
  public void setVariableLocal(@NonNull String name, @Nullable Object value) {
    execution.setVariableLocal(name, value);
  }

  @Override
  public void setVariableTransient(@NonNull String name, @Nullable Object value) {
    ((AbstractVariableScope) execution).setVariableLocalTransient(name, value);
  }

  @Override
  public void removeVariable(@NonNull String name) {
    execution.removeVariable(name);
  }
}
