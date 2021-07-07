package com.epam.digital.data.platform.bpms.camunda.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.config.CamundaProperties;
import com.epam.digital.data.platform.bpms.config.CamundaSystemVariablesSupportListener;
import com.epam.digital.data.platform.bpms.listener.AuthorizationStartEventListener;
import com.epam.digital.data.platform.bpms.listener.CompleterTaskEventListener;
import com.epam.digital.data.platform.bpms.listener.InitiatorTokenStartEventListener;
import java.util.List;
import org.assertj.core.util.Maps;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
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
  private ActivityImpl activity;
  @Mock
  private AuthorizationStartEventListener authorizationStartEventListener;
  @Mock
  private InitiatorTokenStartEventListener initiatorTokenStartEventListener;
  @Mock
  private CompleterTaskEventListener completerTaskEventListener;
  @Mock
  private UserTaskActivityBehavior userTaskActivityBehavior;
  @Mock
  private TaskDefinition taskDefinition;

  private CamundaSystemVariablesSupportListener camundaSystemVariablesSupportListener;

  @Before
  public void init() {
    camundaSystemVariablesSupportListener = new CamundaSystemVariablesSupportListener(
        camundaProperties, authorizationStartEventListener, initiatorTokenStartEventListener,
        completerTaskEventListener);
  }

  @Test
  public void shouldAddListenerThatAddsCamundaSystemPropertiesToBpmn() throws Exception {
    when(camundaProperties.getSystemVariables()).thenReturn(Maps.newHashMap("var1", "value1"));

    camundaSystemVariablesSupportListener.parseStartEvent(null, null, activity);

    ArgumentCaptor<ExecutionListener> captor = ArgumentCaptor.forClass(ExecutionListener.class);
    verify(activity, times(3))
        .addListener(eq(ExecutionListener.EVENTNAME_START), captor.capture());
    List<ExecutionListener> allValues = captor.getAllValues();
    ExecutionListener executionListener = allValues.stream()
        .filter(listener -> !(listener instanceof AuthorizationStartEventListener))
        .filter(listener -> !(listener instanceof InitiatorTokenStartEventListener)).findFirst()
        .get();
    assertThat(executionListener).isNotNull();
    executionListener.notify(delegateExecution);
    verify(delegateExecution).setVariable("var1", "value1");
  }

  @Test
  public void shouldAddTaskListener() {
    when(activity.getActivityBehavior()).thenReturn(userTaskActivityBehavior);
    when(userTaskActivityBehavior.getTaskDefinition()).thenReturn(taskDefinition);

    camundaSystemVariablesSupportListener.parseUserTask(null, null, activity);

    ArgumentCaptor<TaskListener> captor = ArgumentCaptor.forClass(TaskListener.class);
    verify(taskDefinition, times(1))
        .addTaskListener(eq(TaskListener.EVENTNAME_COMPLETE), captor.capture());
  }
}