package com.epam.digital.data.platform.dataaccessor;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BaseVariableAccessorFactoryTest {

  @Mock
  private DelegateExecution delegateExecution;
  @InjectMocks
  private BaseVariableAccessorFactory baseVariableAccessorFactory;

  @Test
  void fromDelegateExecution() {
    var result = baseVariableAccessorFactory.from(delegateExecution);

    assertThat(result).isInstanceOf(DelegateExecutionVariableAccessor.class);
    assertThat(ReflectionTestUtils.getField(result, "execution")).isEqualTo(delegateExecution);
  }

}
