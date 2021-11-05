package com.epam.digital.data.platform.dataaccessor.named;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BaseNamedVariableAccessorFactoryTest {

  private static final String VARIABLE_NAME = "variable";
  private static final boolean VARIABLE_TRANSIENT_FLAG = true;

  private BaseNamedVariableAccessorFactory baseFixedVariableAccessorFactory;
  @Mock
  private VariableAccessorFactory variableAccessorFactory;

  @BeforeEach
  public void setUp() {
    baseFixedVariableAccessorFactory = new BaseNamedVariableAccessorFactory(
        variableAccessorFactory);
  }

  @Test
  void variableAccessor() {
    var result = baseFixedVariableAccessorFactory.variableAccessor(VARIABLE_NAME,
        VARIABLE_TRANSIENT_FLAG);
    assertThat(result).isInstanceOf(BaseNamedVariableAccessor.class);
    assertThat(ReflectionTestUtils.getField(result, "variableName")).isEqualTo(VARIABLE_NAME);
    assertThat(ReflectionTestUtils.getField(result, "isTransient")).isEqualTo(
        VARIABLE_TRANSIENT_FLAG);
  }
}
