package com.epam.digital.data.platform.bpms.listener;

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

  private static final String INITIATOR_TOKEN_VAR_NAME = "initiator_access_token";

  @Override
  public void notify(DelegateExecution execution) {
    var variableScope = (AbstractVariableScope) execution;
    var token = (String) null;

    var auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !(auth.getCredentials() instanceof String)) {
      log.warn("User wasn't authenticated by token... Setting null");
    } else {
      token = (String) auth.getCredentials();
    }

    variableScope.setVariableLocalTransient(INITIATOR_TOKEN_VAR_NAME, token);
  }
}
