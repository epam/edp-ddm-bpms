package com.epam.digital.data.platform.dataaccessor.named;

import com.epam.digital.data.platform.dataaccessor.VariableAccessor;
import lombok.RequiredArgsConstructor;

/**
 * Base implementation of {@link NamedVariableReadAccessor} that is used {@link VariableAccessor}
 * for read access the variable with predefined name
 */
@RequiredArgsConstructor
public class BaseNamedVariableReadAccessor<T> implements NamedVariableReadAccessor<T> {

  private final String name;
  private final VariableAccessor accessor;

  public T get() {
    return accessor.getVariable(name);
  }
}
