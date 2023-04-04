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

package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.general;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.epam.digital.data.platform.starter.security.dto.enums.KeycloakPlatformRole;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is used to add a new
 * roles to the keycloak user.
 */
@RequiredArgsConstructor
@Component(KeycloakSaveUserRoleConnectorDelegate.DELEGATE_NAME)
public class KeycloakSaveUserRoleConnectorDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "keycloakSaveUserRoleConnectorDelegate";
  private static final Map<String, UnaryOperator<List<RoleRepresentation>>> ROLES_BY_TYPE =
      Map.of(
          "REGISTRY ROLES", registryRoles -> registryRoles.stream()
              .filter(Predicate.not(KeycloakSaveUserRoleConnectorDelegate::isPlatformRole))
              .collect(Collectors.toList()),
          "PLATFORM ROLES", platformRoles -> platformRoles.stream()
              .filter(KeycloakSaveUserRoleConnectorDelegate::isPlatformRole)
              .collect(Collectors.toList()),
          "ALL ROLES", roles -> roles
      );

  @Qualifier("officer-keycloak-client-service")
  private final IdmService officerIdmService;
  @Qualifier("citizen-keycloak-client-service")
  private final IdmService citizenIdmService;

  @SystemVariable(name = "realm")
  protected NamedVariableAccessor<String> realmVariable;
  @SystemVariable(name = "roleType")
  protected NamedVariableAccessor<String> roleTypeVariable;
  @SystemVariable(name = "username")
  protected NamedVariableAccessor<String> userNameVariable;
  @SystemVariable(name = "roles")
  protected NamedVariableAccessor<List<String>> rolesVariable;

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Override
  protected void executeInternal(DelegateExecution execution) throws Exception {
    var realm = realmVariable.from(execution).getOrThrow();
    var roleType = roleTypeVariable.from(execution).getOrThrow();
    var userName = userNameVariable.from(execution).getOrThrow();
    var inputRoles = rolesVariable.from(execution).getOrThrow();

    var client = "CITIZEN".equals(realm) ? citizenIdmService : officerIdmService;

    var keycloakRoles = client.getRoleRepresentations();

    var keycloakRolesByType = ROLES_BY_TYPE.get(roleType).apply(keycloakRoles);
    checkRoleMatching(keycloakRolesByType, inputRoles, roleType);
    client.removeRoles(userName, keycloakRolesByType);

    var rolesToAdd = getRoleRepresentationsToAdd(keycloakRolesByType, inputRoles);
    client.addRoles(userName, rolesToAdd);
  }

  private void checkRoleMatching(List<RoleRepresentation> keycloakRoles,
      List<String> rolesCandidateToAdd, String roleType) {
    var keycloakRoleNames = keycloakRoles.stream()
        .map(RoleRepresentation::getName)
        .collect(Collectors.toSet());
    if (!rolesCandidateToAdd.isEmpty() && !keycloakRoleNames.containsAll(rolesCandidateToAdd)) {
      throw new IllegalArgumentException(
          String.format("Input roles: %s do not match the selected type: [%s]", rolesCandidateToAdd,
              roleType));
    }
  }

  private static boolean isPlatformRole(RoleRepresentation role) {
    return KeycloakPlatformRole.containsRole(role.getName());
  }

  private List<RoleRepresentation> getRoleRepresentationsToAdd(
      List<RoleRepresentation> keycloakRoles, List<String> rolesToAdd) {
    return keycloakRoles.stream()
        .filter(roleRepresentation -> rolesToAdd.contains(roleRepresentation.getName()))
        .collect(Collectors.toList());
  }
}
