package com.epam.digital.data.platform.dataaccessor.named;

import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.dataaccessor.VariableAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BaseNamedVariableWriteAccessorTest {

  private static final String VARIABLE_NAME = "variable";
  private static final String VARIABLE_VALUE = "value";

  @Mock
  private VariableAccessor variableAccessor;

  @Test
  void set() {
    var fixedVariableWriteAccessor = new BaseNamedVariableWriteAccessor<String>(VARIABLE_NAME,
        variableAccessor, false);
    fixedVariableWriteAccessor.set(VARIABLE_VALUE);
    verify(variableAccessor).setVariable(VARIABLE_NAME, VARIABLE_VALUE);
  }

  @Test
  void setTransient() {
    var fixedVariableWriteAccessor = new BaseNamedVariableWriteAccessor<String>(VARIABLE_NAME,
        variableAccessor, true);
    fixedVariableWriteAccessor.set(VARIABLE_VALUE);
    verify(variableAccessor).setVariableTransient(VARIABLE_NAME, VARIABLE_VALUE);
  }

  @Test
  void setLocal() {
    var fixedVariableWriteAccessor = new BaseNamedVariableWriteAccessor<String>(VARIABLE_NAME,
        variableAccessor, false);
    fixedVariableWriteAccessor.setLocal(VARIABLE_VALUE);
    verify(variableAccessor).setVariableLocal(VARIABLE_NAME, VARIABLE_VALUE);
  }

  @Test
  void setLocalTransient() {
    var fixedVariableWriteAccessor = new BaseNamedVariableWriteAccessor<String>(VARIABLE_NAME,
        variableAccessor, true);
    fixedVariableWriteAccessor.setLocal(VARIABLE_VALUE);
    verify(variableAccessor).setVariableTransient(VARIABLE_NAME, VARIABLE_VALUE);
  }

  @Test
  void remove() {
    var fixedVariableWriteAccessor = new BaseNamedVariableWriteAccessor<String>(VARIABLE_NAME,
        variableAccessor, true);
    fixedVariableWriteAccessor.remove();
    verify(variableAccessor).removeVariable(VARIABLE_NAME);
  }
}
