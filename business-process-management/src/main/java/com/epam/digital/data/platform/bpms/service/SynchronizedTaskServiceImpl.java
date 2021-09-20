package com.epam.digital.data.platform.bpms.service;

import com.epam.digital.data.platform.bpms.exception.TaskAlreadyInCompletionException;
import com.epam.digital.data.platform.bpms.sync.SynchronizationService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SynchronizedTaskServiceImpl extends TaskServiceImpl {

  private final SynchronizationService synchronizationService;

  @Override
  public void complete(String taskId, Map<String, Object> variables) {
    logTryToCompleteTask(taskId, variables);
    synchronizationService.executeOrThrow(taskId, () -> super.complete(taskId, variables),
        () -> createTaskAlreadyInCompletionException(taskId));
    logTaskIsCompleted(taskId);
  }

  @Override
  public VariableMap completeWithVariablesInReturn(String taskId, Map<String, Object> variables,
      boolean deserializeValues) {
    logTryToCompleteTask(taskId, variables);
    var result = synchronizationService.evaluateOrThrow(taskId,
        () -> super.completeWithVariablesInReturn(taskId, variables, deserializeValues),
        () -> createTaskAlreadyInCompletionException(taskId));
    logTaskIsCompleted(taskId);
    return result;
  }

  private TaskAlreadyInCompletionException createTaskAlreadyInCompletionException(String taskId) {
    return new TaskAlreadyInCompletionException(
        String.format("Task %s already in completion", taskId));
  }

  private void logTryToCompleteTask(String taskId, Map<String, Object> variables) {
    log.info("Trying to execute task {} synchronously by id", taskId);
    log.debug("Task {} variables - {}", taskId, variables);
  }

  private void logTaskIsCompleted(String taskId) {
    log.info("Task {} completed synchronously.", taskId);
  }
}
