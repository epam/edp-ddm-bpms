package com.epam.digital.data.platform.dataaccessor.completer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.VariableAccessor;
import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BaseCompleterVariablesAccessorTest {

  @InjectMocks
  private BaseCompleterVariablesAccessor baseCompleterVariablesAccessor;
  @Mock
  private VariableAccessorFactory variableAccessorFactory;
  @Mock
  private VariableAccessor variableAccessor;
  @Mock
  private DelegateExecution delegateExecution;

  @BeforeEach
  void setUp() {
    when(variableAccessorFactory.from(delegateExecution)).thenReturn(variableAccessor);
  }

  @Test
  void from() {
    var result = baseCompleterVariablesAccessor.from(delegateExecution);

    assertThat(result).isInstanceOf(BaseCompleterVariablesReadWriteAccessor.class);
    assertThat(ReflectionTestUtils.getField(result, "variableAccessor")).isSameAs(variableAccessor);
  }

  @Test
  void on() {
    var result = baseCompleterVariablesAccessor.on(delegateExecution);

    assertThat(result).isInstanceOf(BaseCompleterVariablesReadWriteAccessor.class);
    assertThat(ReflectionTestUtils.getField(result, "variableAccessor")).isSameAs(variableAccessor);
  }
}
