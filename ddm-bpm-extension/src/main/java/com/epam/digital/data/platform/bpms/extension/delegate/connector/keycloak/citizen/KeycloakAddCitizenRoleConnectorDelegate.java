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
import com.epam.digital.data.platform.integration.idm.client.KeycloakAdminClient;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseKeycloakCitizenConnectorDelegate} that
 * is used to add a new role to the keycloak user.
 */
@Component(KeycloakAddCitizenRoleConnectorDelegate.DELEGATE_NAME)
public class KeycloakAddCitizenRoleConnectorDelegate extends
    BaseKeycloakCitizenConnectorDelegate {

  public static final String DELEGATE_NAME = "keycloakAddRoleConnectorDelegate";

  protected KeycloakAddCitizenRoleConnectorDelegate(
      @Qualifier("citizen-keycloak-admin-client") KeycloakAdminClient citizenKeycloakAdminClient,
      KeycloakClientService keycloakClientService) {
    super(citizenKeycloakAdminClient, keycloakClientService);
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var username = userNameVariable.from(execution).get();
    var role = roleVariable.from(execution).get();

    keycloakClientService.addRole(citizenKeycloakAdminClient, username, role);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
