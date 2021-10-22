package com.epam.digital.data.platform.bpms.extension.delegate;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseJavaDelegate implements JavaDelegate {

  public abstract String getDelegateName();

  public void logStartDelegateExecution() {
    var log = getLogger();
    if (log.isDebugEnabled()) {
      log.debug("Delegate {} started execution.", getDelegateName());
    }
  }

  public void logProcessExecution(String operationMessage, String parameter) {
    var log = getLogger();
    if (log.isDebugEnabled()) {
      log.debug("Delegate {} performs the operation: {} {}", getDelegateName(), operationMessage,
          parameter);
    }
  }

  public void logProcessExecution(String operationMessage) {
    logProcessExecution(operationMessage, "");
  }

  public void logDelegateExecution(DelegateExecution delegateExecution, Set<String> inputParameters,
      Set<String> outputParameters) {
    var log = getLogger();
    if (!log.isDebugEnabled()) {
      return;
    }

    var execution = (ExecutionEntity) delegateExecution;
    var processDefinition = execution.getProcessDefinition();
    var processDefinitionKey =
        Objects.isNull(processDefinition) ? null : processDefinition.getKey();
    var processInstanceId = execution.getProcessInstanceId();
    var taskDefinitionKey = execution.getCurrentActivityId();
    var inputParamValues = new HashMap<String, Object>();
    inputParameters.forEach(param -> inputParamValues.put(param, execution.getVariable(param)));
    var outputParamValues = new HashMap<String, Object>();
    outputParameters.forEach(param -> outputParamValues.put(param, execution.getVariable(param)));

    log.debug("Delegate {} was executed.\n"
            + "Process-definition - {},\n"
            + "Process-instance-id - {},\n"
            + "Task-definition - {},\n"
            + "Input-params - {},\n"
            + "Output-params - {}",
        getDelegateName(), processDefinitionKey, processInstanceId, taskDefinitionKey,
        inputParamValues, outputParamValues);
  }

  protected void setTransientResult(DelegateExecution execution, String name, Object value) {
    ((AbstractVariableScope) execution).setVariableLocalTransient(name, value);
  }

  protected void setResult(DelegateExecution execution, String name, Object value) {
    execution.setVariable(name, value);
  }

  private Logger getLogger() {
    return LoggerFactory.getLogger(this.getClass());
  }
}
