package com.epam.digital.data.platform.dataaccessor.initiator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.VariableAccessor;
import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BaseInitiatorVariablesAccessorTest {

  @InjectMocks
  private BaseInitiatorVariablesAccessor baseInitiatorVariablesAccessor;
  @Mock
  private VariableAccessorFactory variableAccessorFactory;
  @Mock
  private VariableAccessor variableAccessor;
  @Mock
  private ExecutionEntity execution;

  @BeforeEach
  void setUp() {
    when(variableAccessorFactory.from(execution)).thenReturn(variableAccessor);
  }

  @Test
  void from() {
    var result = baseInitiatorVariablesAccessor.from(execution);

    assertThat(result).isInstanceOf(BaseInitiatorVariablesReadWriteAccessor.class);
    assertThat(ReflectionTestUtils.getField(result, "variableAccessor")).isSameAs(variableAccessor);
    assertThat(ReflectionTestUtils.getField(result, "execution")).isSameAs(execution);
  }

  @Test
  void on() {
    var result = baseInitiatorVariablesAccessor.on(execution);

    assertThat(result).isInstanceOf(BaseInitiatorVariablesReadWriteAccessor.class);
    assertThat(ReflectionTestUtils.getField(result, "variableAccessor")).isSameAs(variableAccessor);
    assertThat(ReflectionTestUtils.getField(result, "execution")).isSameAs(execution);
  }
}
