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

import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;

import com.epam.digital.data.platform.bpms.security.CamundaImpersonationFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.ProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link ExecutionListener} listener that is used to set
 * authorizations for current user before process instance starts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationStartEventListener implements ExecutionListener {

  @Qualifier("camundaAdminImpersonationFactory")
  private final CamundaImpersonationFactory camundaImpersonationFactory;

  @Override
  public void notify(DelegateExecution execution) throws Exception {
    log.debug("AuthorizationStartEventListener started...");
    var optionalCamundaImpersonation = camundaImpersonationFactory.getCamundaImpersonation();
    if (optionalCamundaImpersonation.isEmpty()) {
      return;
    }
    var camundaImpersonation = optionalCamundaImpersonation.get();
    var processEngine = camundaImpersonation.getProcessEngine();
    var impersonator = camundaImpersonation.getImpersonator();

    //only admin user has rights for creation authorizations
    try {
      camundaImpersonation.impersonate();
      addPermissionForProcessInstances(processEngine, execution, impersonator.getUserId());
    } finally {
      camundaImpersonation.revertToSelf();
    }
    log.debug("AuthorizationStartEventListener finished...");
  }

  private void addPermissionForProcessInstances(ProcessEngine processEngine,
      DelegateExecution execution, String userId) {
    Authorization authorization = createAuthorization(processEngine,
        execution.getProcessInstanceId(),
        Resources.PROCESS_INSTANCE, new Permission[]{ProcessInstancePermissions.READ,
            ProcessInstancePermissions.UPDATE_VARIABLE}, userId);
    processEngine.getAuthorizationService().saveAuthorization(authorization);
  }

  private Authorization createAuthorization(ProcessEngine processEngine, String resourceId,
      Resources resources, Permission[] permissions, String userId) {
    Authorization authorization = processEngine.getAuthorizationService()
        .createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setResource(resources);
    authorization.setResourceId(resourceId);
    authorization.setPermissions(permissions);
    authorization.setUserId(userId);
    return authorization;
  }
}
