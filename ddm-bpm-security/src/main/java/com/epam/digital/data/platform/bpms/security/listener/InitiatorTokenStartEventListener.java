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
