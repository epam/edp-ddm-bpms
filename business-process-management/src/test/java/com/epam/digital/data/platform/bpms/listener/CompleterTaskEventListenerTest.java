package com.epam.digital.data.platform.bpms.listener;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class CompleterTaskEventListenerTest {

  @InjectMocks
  private CompleterTaskEventListener completerTaskEventListener;
  @Mock
  private TaskEntity taskEntity;
  @Mock
  private ExecutionEntity executionEntity;

  @Test
  public void testNotAuthenticatedCompleter() {
    SecurityContextHolder.getContext().setAuthentication(null);
    when(taskEntity.getExecution()).thenReturn(executionEntity);

    completerTaskEventListener.notify(taskEntity);

    verify(executionEntity, times(1)).setVariable("null_completer", null);
    verify(executionEntity, times(1))
        .setVariableLocalTransient("null_completer_access_token", null);
  }

  @Test
  public void testAuthenticatedCompleter() {
    var userName = "user_name";
    var token = "access_token";
    var taskDefinitionKey = "task_key";
    var auth = new UsernamePasswordAuthenticationToken(userName, token);
    SecurityContextHolder.getContext().setAuthentication(auth);
    when(taskEntity.getExecution()).thenReturn(executionEntity);
    when(taskEntity.getTaskDefinitionKey()).thenReturn(taskDefinitionKey);

    completerTaskEventListener.notify(taskEntity);

    verify(executionEntity, times(1))
        .setVariable(String.format("%s_completer", taskDefinitionKey), userName);
    verify(executionEntity, times(1))
        .setVariableLocalTransient(String.format("%s_completer_access_token", taskDefinitionKey), token);
  }
}
