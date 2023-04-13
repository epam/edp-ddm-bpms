/*
 * Copyright 2023 EPAM Systems.
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

package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak;

import static java.util.stream.Collectors.toList;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is used to get user roles
 * from keycloak.
 */
@RequiredArgsConstructor
@Component(KeycloakGetUserRoleConnectorDelegate.DELEGATE_NAME)
public class KeycloakGetUserRoleConnectorDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "keycloakGetUserRoleConnectorDelegate";

  private final IdmServiceProvider provider;
  @SystemVariable(name = "realm")
  private NamedVariableAccessor<String> realmVariable;
  @SystemVariable(name = "roleType")
  private NamedVariableAccessor<String> roleTypeVariable;
  @SystemVariable(name = "username")
  private NamedVariableAccessor<String> userNameVariable;
  @SystemVariable(name = "response", isTransient = true)
  private NamedVariableAccessor<List<String>> responseVariable;

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Override
  protected void executeInternal(DelegateExecution execution) throws Exception {
    var realm = realmVariable.from(execution).getOrThrow();
    var roleType = roleTypeVariable.from(execution).getOrThrow();
    var userName = userNameVariable.from(execution).getOrThrow();

    var client = provider.getIdmService(realm);

    var userRoles = client.getUserRoles(userName);
    var keycloakRolesByType = UserRoleDelegateUtils.ROLES_BY_TYPE.get(roleType)
        .apply(userRoles);

    var listRoles = keycloakRolesByType.stream().map(RoleRepresentation::getName)
        .collect(toList());
    responseVariable.on(execution).set(listRoles);
  }
}
