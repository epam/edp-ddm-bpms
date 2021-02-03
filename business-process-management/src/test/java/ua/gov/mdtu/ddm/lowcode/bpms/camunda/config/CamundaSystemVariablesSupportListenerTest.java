package ua.gov.mdtu.ddm.lowcode.bpms.camunda.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
  @Mock
  private AuthorizationStartEventListener authorizationStartEventListener;

  private CamundaSystemVariablesSupportListener camundaSystemVariablesSupportListener;

  @Before
  public void init() {
    camundaSystemVariablesSupportListener = new CamundaSystemVariablesSupportListener(
        camundaProperties, authorizationStartEventListener);
  }

  @Test
  public void shouldAddListenerThatAddsCamundaSystemPropertiesToBpmn() throws Exception {
    when(camundaProperties.getSystemVariables()).thenReturn(Maps.newHashMap("var1", "value1"));

    camundaSystemVariablesSupportListener.parseStartEvent(null, null, startEventActivity);

    ArgumentCaptor<ExecutionListener> captor = ArgumentCaptor.forClass(ExecutionListener.class);
    verify(startEventActivity, times(2)).addListener(eq(ExecutionListener.EVENTNAME_START), captor.capture());
    List<ExecutionListener> allValues = captor.getAllValues();
    ExecutionListener executionListener = allValues.stream()
        .filter(listener -> !(listener instanceof AuthorizationStartEventListener)).findFirst().get();
    assertThat(executionListener).isNotNull();
    executionListener.notify(delegateExecution);
    verify(delegateExecution).setVariable("var1", "value1");
  }
}