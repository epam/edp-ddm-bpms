/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.bpms.security.listener;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.security.listener.CompleterTaskEventListener;
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
public class CompleterTaskEventListenerTest {

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
