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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SynchronizedTaskServiceImplTest {

  @Mock
  private SynchronizationService synchronizationService;
  @Mock
  private CommandExecutor commandExecutor;
  @InjectMocks
  private SynchronizedTaskServiceImpl service;

  @Before
  public void setUp() {
    service.setCommandExecutor(commandExecutor);
  }

  @Test
  public void testCompleteTask() {
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
  public void testCompleteTaskAndReturn() {
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
