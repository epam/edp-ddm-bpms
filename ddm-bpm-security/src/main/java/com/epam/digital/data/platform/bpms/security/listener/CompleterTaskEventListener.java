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

import com.epam.digital.data.platform.dataaccessor.completer.CompleterVariablesAccessor;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link TaskListener} listener that is used to set
 * completer token and username after task completion.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompleterTaskEventListener implements TaskListener {

  private final CompleterVariablesAccessor completerVariablesAccessor;

  @Override
  public void notify(DelegateTask delegateTask) {
    var taskDefinitionKey = delegateTask.getTaskDefinitionKey();
    if (Objects.isNull(taskDefinitionKey)) {
      return;
    }
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      log.warn("User is not authenticated");
      return;
    }
    var completerToken = (String) authentication.getCredentials();
    var completerName = authentication.getName();

    var delegateExecution = getRootExecution(delegateTask);
    var variableAccessor = completerVariablesAccessor.on(delegateExecution);
    variableAccessor.setTaskCompleter(taskDefinitionKey, completerName);
    variableAccessor.setTaskCompleterToken(taskDefinitionKey, completerToken);

    log.debug("Setting task completer variables:\n"
        + "Task definition key - {}\n"
        + "User name - {}", taskDefinitionKey, completerName);
  }

  private DelegateExecution getRootExecution(DelegateTask delegateTask) {
    var variableScope = (ExecutionEntity) delegateTask.getExecution();
    while (variableScope.getParentVariableScope() != null) {
      variableScope = (ExecutionEntity) variableScope.getParentVariableScope();
    }
    return variableScope;
  }
}
