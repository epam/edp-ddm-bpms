package ua.gov.mdtu.ddm.lowcode.bpms.camunda.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.assertj.core.util.Maps;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CamundaSystemVariablesSupportListenerTest {

  @Mock
  private CamundaProperties camundaProperties;
  @Mock
  private DelegateExecution delegateExecution;
  @Mock
  private ActivityImpl startEventActivity;

  private CamundaSystemVariablesSupportListener camundaSystemVariablesSupportListener;

  @Before
  public void init() {
    camundaSystemVariablesSupportListener = new CamundaSystemVariablesSupportListener(
        camundaProperties);
  }

  @Test
  public void shouldAddListenerThatAddsCamundaSystemPropertiesToBpmn() throws Exception {
    //given
    when(camundaProperties.getSystemVariables()).thenReturn(Maps.newHashMap("var1", "value1"));
    //when
    camundaSystemVariablesSupportListener.parseStartEvent(null, null, startEventActivity);
    //then
    ArgumentCaptor<ExecutionListener> captor = ArgumentCaptor.forClass(ExecutionListener.class);
    verify(startEventActivity).addListener(eq(ExecutionListener.EVENTNAME_START), captor.capture());
    ExecutionListener listener = captor.getValue();
    assertThat(listener).isNotNull();
    listener.notify(delegateExecution);
    verify(delegateExecution).setVariable("var1", "value1");
  }
}