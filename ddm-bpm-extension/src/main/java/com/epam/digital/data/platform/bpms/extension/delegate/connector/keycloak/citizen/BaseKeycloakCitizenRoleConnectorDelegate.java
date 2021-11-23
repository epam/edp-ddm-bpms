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

package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.citizen;

import com.epam.digital.data.platform.bpms.extension.service.KeycloakClientService;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to provide common
 * logic for working with the keycloak client to perform operations with user role
 */
public abstract class BaseKeycloakCitizenRoleConnectorDelegate extends
    BaseKeycloakCitizenConnectorDelegate {

  @SystemVariable(name = "user_name")
  private NamedVariableAccessor<String> userNameVariable;
  @SystemVariable(name = "role")
  private NamedVariableAccessor<String> roleVariable;

  protected BaseKeycloakCitizenRoleConnectorDelegate(KeycloakClientService keycloakClientService) {
    super(keycloakClientService);
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var userName = userNameVariable.from(execution).get();
    var role = roleVariable.from(execution).get();

    var realmResource = keycloakClientService.getRealmResource();
    var roleRepresentation = keycloakClientService.getRoleRepresentation(realmResource, role);
    var userRepresentation = keycloakClientService.getUserRepresentation(realmResource, userName);
    var roleScopeResource = keycloakClientService
        .getRoleScopeResource(realmResource, userRepresentation.getId());

    performOperationWithRole(roleScopeResource, roleRepresentation);
  }

  protected abstract void performOperationWithRole(RoleScopeResource roleScopeResource,
      RoleRepresentation roleRepresentation);
}
