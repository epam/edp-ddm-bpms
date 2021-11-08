package com.epam.digital.data.platform.bpms.security.config;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.security.listener.AuthorizationStartEventListener;
import com.epam.digital.data.platform.bpms.security.listener.CompleterTaskEventListener;
import com.epam.digital.data.platform.bpms.security.listener.InitiatorTokenStartEventListener;
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
public class BpmSecuritySupportListenerTest {

  @Mock
  private ActivityImpl activity;
  @Mock
  private UserTaskActivityBehavior userTaskActivityBehavior;
  @Mock
  private AuthorizationStartEventListener authorizationStartEventListener;
  @Mock
  private InitiatorTokenStartEventListener initiatorTokenStartEventListener;
  @Mock
  private CompleterTaskEventListener completerTaskEventListener;
  @Mock
  private TaskDefinition taskDefinition;

  private BpmSecuritySupportListener bpmSupportListener;

  @Before
  public void init() {
    bpmSupportListener = new BpmSecuritySupportListener(authorizationStartEventListener,
        initiatorTokenStartEventListener, completerTaskEventListener);
  }

  @Test
  public void shouldAddTaskListener() {
    when(activity.getActivityBehavior()).thenReturn(userTaskActivityBehavior);
    when(userTaskActivityBehavior.getTaskDefinition()).thenReturn(taskDefinition);

    bpmSupportListener.parseUserTask(null, null, activity);

    ArgumentCaptor<TaskListener> captor = ArgumentCaptor.forClass(TaskListener.class);
    verify(taskDefinition, times(1))
        .addTaskListener(eq(TaskListener.EVENTNAME_COMPLETE), captor.capture());
  }

  @Test
  public void shouldAddStartEventListeners() {
    bpmSupportListener.parseStartEvent(null, null, activity);

    ArgumentCaptor<ExecutionListener> captor = ArgumentCaptor.forClass(ExecutionListener.class);
    verify(activity, times(2))
        .addListener(eq(ExecutionListener.EVENTNAME_START), captor.capture());
  }
}