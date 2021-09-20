package com.epam.digital.data.platform.bpms.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link TaskListener} listener that is used to set
 * completer token and username after task completion.
 */
@Slf4j
@Component
public class CompleterTaskEventListener implements TaskListener {

  private static final String COMPLETER_VAR_TOKEN_FORMAT = "%s_completer_access_token";
  private static final String COMPLETER_VAR_NAME_FORMAT = "%s_completer";

  @Override
  public void notify(DelegateTask delegateTask) {
    var taskDefinitionKey = delegateTask.getTaskDefinitionKey();
    AbstractVariableScope variableScope = getRootExecution(delegateTask);
    String completerToken = null;
    String completerName = null;
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      log.warn("User is not authenticated");
    } else {
      completerToken = (String) authentication.getCredentials();
      completerName = authentication.getName();
    }
    var completerVarResultName = String.format(COMPLETER_VAR_NAME_FORMAT, taskDefinitionKey);
    var completerVarResultToken = String.format(COMPLETER_VAR_TOKEN_FORMAT, taskDefinitionKey);
    variableScope.setVariable(completerVarResultName, completerName);
    variableScope.setVariableLocalTransient(completerVarResultToken, completerToken);
    log.debug("Setting task completer variables:\n"
            + "Task definition key - {}\n"
            + "User name {} - {}\n"
            + "Access token {} - {}", taskDefinitionKey, completerVarResultName, completerName,
        completerVarResultToken, completerToken);
  }

  private AbstractVariableScope getRootExecution(DelegateTask delegateTask) {
    var variableScope = (AbstractVariableScope) delegateTask.getExecution();
    while (variableScope.getParentVariableScope() != null) {
      variableScope = variableScope.getParentVariableScope();
    }
    return variableScope;
  }
}
