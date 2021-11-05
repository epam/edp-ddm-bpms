package com.epam.digital.data.platform.bpms.extension.delegate;

import java.util.Objects;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;
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
