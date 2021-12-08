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

package com.epam.digital.data.platform.bpms.extension.delegate;

import java.util.Objects;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.LoggerFactory;

public abstract class BaseJavaDelegate implements JavaDelegate {

  public abstract String getDelegateName();

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    logStartDelegateExecution();
    executeInternal(execution);
    logDelegateExecution(execution);
  }

  @SuppressWarnings("java:S112")
  protected abstract void executeInternal(DelegateExecution execution) throws Exception;

  private void logStartDelegateExecution() {
    var log = LoggerFactory.getLogger(this.getClass());
    if (log.isDebugEnabled()) {
      log.debug("Starting execution");
    }
  }

  private void logDelegateExecution(DelegateExecution delegateExecution) {
    var log = LoggerFactory.getLogger(this.getClass());
    if (!log.isDebugEnabled()) {
      return;
    }

    var execution = (ExecutionEntity) delegateExecution;
    var processDefinition = execution.getProcessDefinition();
    var processDefinitionKey =
        Objects.isNull(processDefinition) ? null : processDefinition.getKey();
    var processInstanceId = execution.getProcessInstanceId();
    var taskDefinitionKey = execution.getCurrentActivityId();

    log.debug("Delegate {} was executed.\n"
            + "Process-definition - {},\n"
            + "Process-instance-id - {},\n"
            + "Task-definition - {}",
        getDelegateName(), processDefinitionKey, processInstanceId, taskDefinitionKey);
  }
}
