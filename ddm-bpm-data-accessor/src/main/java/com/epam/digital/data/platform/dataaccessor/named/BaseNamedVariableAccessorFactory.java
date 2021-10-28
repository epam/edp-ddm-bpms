package com.epam.digital.data.platform.dataaccessor.named;

import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

/**
 * Base implementation of {@link NamedVariableAccessorFactory} Spring bean that creates {@link
 * BaseNamedVariableAccessor}
 */
@RequiredArgsConstructor
public class BaseNamedVariableAccessorFactory implements NamedVariableAccessorFactory {

  private final VariableAccessorFactory variableAccessorFactory;

  @NonNull
  public <T> NamedVariableAccessor<T> variableAccessor(String name, boolean isTransient) {
    return new BaseNamedVariableAccessor<>(name, isTransient, variableAccessorFactory);
  }
}
