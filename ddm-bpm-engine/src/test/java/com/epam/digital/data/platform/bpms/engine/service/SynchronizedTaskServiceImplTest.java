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

package com.epam.digital.data.platform.bpms.engine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.bpms.engine.exception.TaskAlreadyInCompletionException;
import com.epam.digital.data.platform.bpms.engine.sync.SynchronizationService;
import java.util.function.Supplier;
import org.camunda.bpm.engine.impl.cmd.CompleteTaskCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SynchronizedTaskServiceImplTest {

  @InjectMocks
  private SynchronizedTaskServiceImpl service;
  @Mock
  private SynchronizationService synchronizationService;
  @Mock
  private CommandExecutor commandExecutor;

  @BeforeEach
  void setUp() {
    service.setCommandExecutor(commandExecutor);
  }

  @Test
  void testCompleteTask() {
    var taskId = "taskId";

    doAnswer(invocation -> {
      Runnable runnable = invocation.getArgument(1);
      runnable.run();
      return null;
    }).when(synchronizationService).executeOrThrow(eq(taskId), any(), any());

    service.complete(taskId, null);

    verify(commandExecutor).execute(refEq(new CompleteTaskCmd(taskId, null, false, false)));
  }

  @Test
  void testCompleteTaskAndReturn() {
    var taskId = "taskId";

    doAnswer(invocation -> {
      Supplier<TaskAlreadyInCompletionException> supplier = invocation.getArgument(2);
      throw supplier.get();
    }).when(synchronizationService).evaluateOrThrow(eq(taskId), any(), any());

    var ex = assertThrows(TaskAlreadyInCompletionException.class,
        () -> service.completeWithVariablesInReturn(taskId, null, true));

    assertThat(ex.getMessage()).isEqualTo("Task " + taskId + " already in completion");
    verify(commandExecutor, never()).execute(any());
  }
}
