package ua.gov.mdtu.ddm.lowcode.bpms.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.CamundaSystemException;

@RunWith(MockitoJUnitRunner.class)
public class CamundaSystemErrorDelegateTest {

  @InjectMocks
  private CamundaSystemErrorDelegate delegate;
  @Mock
  private DelegateExecution delegateExecution;

  @Test
  public void testEmptyCamundaSystemError() {
    when(delegateExecution.hasVariable("systemError")).thenReturn(false);

    var exception = assertThrows(CamundaSystemException.class,
        () -> delegate.execute(delegateExecution));

    assertThat(exception.getMessage()).isEqualTo(StringUtils.EMPTY);
  }
}
