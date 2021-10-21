package com.epam.digital.data.platform.bpms.security.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link ExecutionListener} listener that is used to set
 * authorizations for current user before process instance starts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitiatorTokenStartEventListener implements ExecutionListener {

  public static final String INITIATOR_TOKEN_VAR_NAME = "initiator_access_token";

  @Override
  @SuppressWarnings("findbugs:BC_UNCONFIRMED_CAST")
  public void notify(DelegateExecution execution) {
    var variableScope = (AbstractVariableScope) execution;
    String token = null;

    var auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !(auth.getCredentials() instanceof String)) {
      log.warn("User wasn't authenticated by token... Setting null");
    } else {
      token = (String) auth.getCredentials();
    }

    variableScope.setVariableLocalTransient(INITIATOR_TOKEN_VAR_NAME, token);
    log.debug("Setting initiator access token {}={}. ProcessDefinitionId={}, processInstanceId={}",
        INITIATOR_TOKEN_VAR_NAME, token, execution.getProcessDefinitionId(),
        execution.getProcessInstanceId());
  }
}
