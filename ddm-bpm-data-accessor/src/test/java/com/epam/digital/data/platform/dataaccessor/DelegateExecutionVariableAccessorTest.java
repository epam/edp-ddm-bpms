package com.epam.digital.data.platform.dataaccessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelegateExecutionVariableAccessorTest {

  private static final String VARIABLE_NAME = "variable";
  private static final String VARIABLE_VALUE = "value";

  @InjectMocks
  private DelegateExecutionVariableAccessor delegateExecutionVariableAccessor;
  @Mock
  private ExecutionEntity delegateExecution;

  @Test
  void getVariable() {
    when(delegateExecution.getVariable(VARIABLE_NAME)).thenReturn(VARIABLE_VALUE);
    assertThat((String) delegateExecutionVariableAccessor.getVariable(VARIABLE_NAME))
        .isEqualTo(VARIABLE_VALUE);

    when(delegateExecution.getVariable(VARIABLE_NAME)).thenReturn(null);
    assertThat((String) delegateExecutionVariableAccessor.getVariable(VARIABLE_NAME)).isNull();
  }

  @Test
  void setVariable() {
    delegateExecutionVariableAccessor.setVariable(VARIABLE_NAME, VARIABLE_VALUE);

    verify(delegateExecution).setVariable(VARIABLE_NAME, VARIABLE_VALUE);
  }

  @Test
  void setVariableLocal() {
    delegateExecutionVariableAccessor.setVariableLocal(VARIABLE_NAME, VARIABLE_VALUE);

    verify(delegateExecution).setVariableLocal(VARIABLE_NAME, VARIABLE_VALUE);
  }

  @Test
  void setVariableTransient() {
    delegateExecutionVariableAccessor.setVariableTransient(VARIABLE_NAME, VARIABLE_VALUE);

    verify(delegateExecution).setVariableLocalTransient(VARIABLE_NAME, VARIABLE_VALUE);
  }

  @Test
  void removeVariable() {
    delegateExecutionVariableAccessor.removeVariable(VARIABLE_NAME);

    verify(delegateExecution).removeVariable(VARIABLE_NAME);
  }
}
