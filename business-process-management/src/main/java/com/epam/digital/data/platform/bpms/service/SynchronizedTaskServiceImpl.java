package com.epam.digital.data.platform.bpms.service;

import com.epam.digital.data.platform.bpms.exception.TaskAlreadyInCompletionException;
import com.epam.digital.data.platform.bpms.sync.SynchronizationService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SynchronizedTaskServiceImpl extends TaskServiceImpl {

  private final SynchronizationService synchronizationService;

  @Override
  public void complete(String taskId, Map<String, Object> variables) {
    synchronizationService.executeOrThrow(taskId, () -> super.complete(taskId, variables),
        () -> createTaskAlreadyInCompletionException(taskId));
  }

  @Override
  public VariableMap completeWithVariablesInReturn(String taskId, Map<String, Object> variables,
      boolean deserializeValues) {
    return synchronizationService.evaluateOrThrow(taskId,
        () -> super.completeWithVariablesInReturn(taskId, variables, deserializeValues),
        () -> createTaskAlreadyInCompletionException(taskId));
  }

  private TaskAlreadyInCompletionException createTaskAlreadyInCompletionException(String taskId) {
    return new TaskAlreadyInCompletionException(
        String.format("Task %s already in completion", taskId));
  }
}
