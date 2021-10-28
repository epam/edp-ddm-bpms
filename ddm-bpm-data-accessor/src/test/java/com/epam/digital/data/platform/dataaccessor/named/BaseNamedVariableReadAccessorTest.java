package com.epam.digital.data.platform.dataaccessor.named;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.VariableAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BaseNamedVariableReadAccessorTest {

  private static final String VARIABLE_NAME = "variable";
  private static final String VARIABLE_VALUE = "value";

  @Mock
  private VariableAccessor variableAccessor;

  private BaseNamedVariableReadAccessor<String> fixedVariableReadAccessor;

  @BeforeEach
  public void setUp() {
    fixedVariableReadAccessor = new BaseNamedVariableReadAccessor<>(VARIABLE_NAME,
        variableAccessor);
  }

  @Test
  void get() {
    when(variableAccessor.getVariable(VARIABLE_NAME)).thenReturn(VARIABLE_VALUE);
    assertThat(fixedVariableReadAccessor.get()).isSameAs(VARIABLE_VALUE);
  }

  @Test
  void getOptional() {
    when(variableAccessor.getVariable(VARIABLE_NAME)).thenReturn(null);
    assertThat(fixedVariableReadAccessor.getOptional()).isEmpty();
  }

  @Test
  void getOrDefault() {
    when(variableAccessor.getVariable(VARIABLE_NAME)).thenReturn(null);
    assertThat(fixedVariableReadAccessor.getOrDefault("default")).isEqualTo("default");
  }
}
