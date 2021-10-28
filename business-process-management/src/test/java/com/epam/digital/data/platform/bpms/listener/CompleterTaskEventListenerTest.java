package com.epam.digital.data.platform.bpms.listener;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.completer.CompleterVariablesAccessor;
import com.epam.digital.data.platform.dataaccessor.completer.CompleterVariablesWriteAccessor;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CompleterTaskEventListenerTest {

  @InjectMocks
  private CompleterTaskEventListener completerTaskEventListener;
  @Mock
  private TaskEntity taskEntity;
  @Mock
  private ExecutionEntity executionEntity;
  @Mock
  private CompleterVariablesAccessor completerVariablesAccessor;
  @Mock
  private CompleterVariablesWriteAccessor completerVariablesWriteAccessor;

  @Test
  void testNotDefinedTaskDefinitionKey() {
    completerTaskEventListener.notify(taskEntity);

    verify(completerVariablesWriteAccessor, never()).setTaskCompleter(anyString(), anyString());
    verify(completerVariablesWriteAccessor, never()).setTaskCompleterToken(anyString(),
        anyString());
  }

  @Test
  void testNotAuthenticatedCompleter() {
    when(taskEntity.getTaskDefinitionKey()).thenReturn("task_key");
    SecurityContextHolder.getContext().setAuthentication(null);

    completerTaskEventListener.notify(taskEntity);

    verify(completerVariablesWriteAccessor, never()).setTaskCompleter(anyString(), anyString());
    verify(completerVariablesWriteAccessor, never()).setTaskCompleterToken(anyString(),
        anyString());
  }

  @Test
  void testAuthenticatedCompleter() {
    var userName = "user_name";
    var token = "access_token";
    var taskDefinitionKey = "task_key";
    var auth = new UsernamePasswordAuthenticationToken(userName, token);
    SecurityContextHolder.getContext().setAuthentication(auth);
    when(taskEntity.getExecution()).thenReturn(executionEntity);
    when(taskEntity.getTaskDefinitionKey()).thenReturn(taskDefinitionKey);
    when(completerVariablesAccessor.on(executionEntity)).thenReturn(
        completerVariablesWriteAccessor);

    completerTaskEventListener.notify(taskEntity);

    verify(completerVariablesWriteAccessor).setTaskCompleter(taskDefinitionKey, userName);
    verify(completerVariablesWriteAccessor).setTaskCompleterToken(taskDefinitionKey, token);
  }
}
