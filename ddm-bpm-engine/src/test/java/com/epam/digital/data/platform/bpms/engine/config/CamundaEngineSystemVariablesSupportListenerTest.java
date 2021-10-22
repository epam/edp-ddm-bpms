package com.epam.digital.data.platform.bpms.engine.config;

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
public class CamundaEngineSystemVariablesSupportListenerTest {

  @Mock
  private CamundaProperties camundaProperties;
  @Mock
  private DelegateExecution delegateExecution;
  @Mock
  private ActivityImpl activity;

  private CamundaEngineSystemVariablesSupportListener camundaSystemVariablesSupportListener;

  @Before
  public void init() {
    camundaSystemVariablesSupportListener = new CamundaEngineSystemVariablesSupportListener(
        camundaProperties);
  }

  @Test
  public void shouldAddListenerThatAddsCamundaSystemPropertiesToBpmn() throws Exception {
    when(camundaProperties.getSystemVariables()).thenReturn(Maps.newHashMap("var1", "value1"));

    camundaSystemVariablesSupportListener.parseStartEvent(null, null, activity);

    ArgumentCaptor<ExecutionListener> captor = ArgumentCaptor.forClass(ExecutionListener.class);
    verify(activity, times(1))
        .addListener(eq(ExecutionListener.EVENTNAME_START), captor.capture());
    List<ExecutionListener> allValues = captor.getAllValues();
    ExecutionListener executionListener = allValues.stream().findFirst().get();
    assertThat(executionListener).isNotNull();
    executionListener.notify(delegateExecution);
    verify(delegateExecution).setVariable("var1", "value1");
  }
}
