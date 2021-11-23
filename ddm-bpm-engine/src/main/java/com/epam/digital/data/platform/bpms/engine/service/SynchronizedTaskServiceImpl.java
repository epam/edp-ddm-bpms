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

import com.epam.digital.data.platform.bpms.engine.exception.TaskAlreadyInCompletionException;
import com.epam.digital.data.platform.bpms.engine.sync.SynchronizationService;
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
