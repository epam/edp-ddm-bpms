package com.epam.digital.data.platform.dataaccessor.named;

import com.epam.digital.data.platform.dataaccessor.VariableAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

/**
 * Base implementation of {@link NamedVariableWriteAccessor} that is used {@link VariableAccessor}
 * for write access the variable with predefined name
 */
@RequiredArgsConstructor
public class BaseNamedVariableWriteAccessor<T> implements NamedVariableWriteAccessor<T> {

  private final String name;
  private final VariableAccessor accessor;
  private final boolean isTransient;

  @Override
  public void set(@Nullable T value) {
    if (isTransient) {
      accessor.setVariableTransient(name, value);
    } else {
      accessor.setVariable(name, value);
    }
  }

  @Override
  public void setLocal(@Nullable T value) {
    if (isTransient) {
      accessor.setVariableTransient(name, value);
    } else {
      accessor.setVariableLocal(name, value);
    }
  }

  @Override
  public void remove() {
    accessor.removeVariable(name);
  }
}
