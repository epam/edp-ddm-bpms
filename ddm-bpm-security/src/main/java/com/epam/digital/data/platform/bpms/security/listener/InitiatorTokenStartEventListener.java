package com.epam.digital.data.platform.bpms.security.listener;

import com.epam.digital.data.platform.dataaccessor.initiator.InitiatorVariablesAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
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

  private final InitiatorVariablesAccessor initiatorVariablesAccessor;

  @Override
  public void notify(DelegateExecution execution) {
    String token = null;

    var auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !(auth.getCredentials() instanceof String)) {
      log.warn("User wasn't authenticated by token... Setting null");
    } else {
      token = (String) auth.getCredentials();
    }

    initiatorVariablesAccessor.on(execution).setInitiatorAccessToken(token);
    log.debug("Setting initiator access token. ProcessDefinitionId={}, processInstanceId={}",
        execution.getProcessDefinitionId(), execution.getProcessInstanceId());
  }
}
