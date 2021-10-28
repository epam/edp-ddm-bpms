package com.epam.digital.data.platform.bpms.engine.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.VariableAccessor;
import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import org.assertj.core.util.Maps;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CamundaEngineSystemVariablesSupportListenerTest {

  @InjectMocks
  private CamundaEngineSystemVariablesSupportListener camundaSystemVariablesSupportListener;
  @Mock
  private CamundaProperties camundaProperties;
  @Mock
  private VariableAccessorFactory variableAccessorFactory;

  @Mock
  private DelegateExecution delegateExecution;
  @Mock
  private ActivityImpl activity;
  @Mock
  private VariableAccessor variableAccessor;

  @Test
  void shouldAddListenerThatAddsCamundaSystemPropertiesToBpmn() throws Exception {
    when(camundaProperties.getSystemVariables()).thenReturn(Maps.newHashMap("var1", "value1"));
    when(variableAccessorFactory.from(delegateExecution)).thenReturn(variableAccessor);

    camundaSystemVariablesSupportListener.parseStartEvent(null, null, activity);

    var captor = ArgumentCaptor.forClass(ExecutionListener.class);
    verify(activity).addListener(eq(ExecutionListener.EVENTNAME_START), captor.capture());
    var allValues = captor.getAllValues();
    var executionListener = allValues.stream().findFirst().get();
    assertThat(executionListener).isNotNull();
    executionListener.notify(delegateExecution);
    verify(variableAccessor).setVariable("var1", "value1");
  }
}
