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

package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.KeycloakUserDto;
import com.epam.digital.data.platform.bpms.extension.service.KeycloakClientService;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to get the list of
 * users from keycloak by role.
 */
@Component(KeycloakGetOfficerUsersConnectorDelegate.DELEGATE_NAME)
public class KeycloakGetOfficerUsersConnectorDelegate extends BaseKeycloakOfficerConnectorDelegate {

  public static final String DELEGATE_NAME = "keycloakGetUsersConnectorDelegate";

  private static final String DEFAULT_ROLE = "officer";

  @SystemVariable(name = "role_name")
  private NamedVariableAccessor<String> roleNameVariable;
  @SystemVariable(name = "usersByRole")
  private NamedVariableAccessor<List<KeycloakUserDto>> usersByRoleVariable;

  public KeycloakGetOfficerUsersConnectorDelegate(
      @Qualifier("officer-keycloak-service") KeycloakClientService keycloakClientService) {
    super(keycloakClientService);
  }

  @Override
  public void executeInternal(DelegateExecution execution) throws Exception {
    var role = roleNameVariable.from(execution).getOrDefault(DEFAULT_ROLE);

    var realmResource = keycloakClientService.getRealmResource();
    var roleUserMembers = keycloakClientService.getRoleUserMembers(realmResource, role);

    usersByRoleVariable.on(execution).set(roleUserMembers);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
