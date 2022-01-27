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

package com.epam.digital.data.platform.bpms.extension.service;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.KeycloakUserDto;
import com.epam.digital.data.platform.integration.idm.client.KeycloakAdminClient;
import com.epam.digital.data.platform.integration.idm.exception.KeycloakException;
import com.epam.digital.data.platform.starter.security.dto.constants.KeycloakSystemAttribute;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

@Slf4j
@RequiredArgsConstructor
public class KeycloakClientServiceImpl implements KeycloakClientService {

  public List<RoleRepresentation> getKeycloakRoles(KeycloakAdminClient client) {
    var realmResource = client.getRealmResource();
    return client.getKeycloakRoles(realmResource);
  }

  public void removeRole(KeycloakAdminClient client, String username, String role) {
    var realmResource = client.getRealmResource();
    var roleRepresentation = client.getRoleRepresentation(realmResource, role);
    var userRepresentation = this.getUserRepresentation(client, realmResource, username);
    var roleScopeResource = client.getRoleScopeResource(realmResource, userRepresentation.getId());
    client.removeRole(roleScopeResource, roleRepresentation);
  }

  public void addRole(KeycloakAdminClient client, String username, String role) {
    var realmResource = client.getRealmResource();
    var roleRepresentation = client.getRoleRepresentation(realmResource, role);
    var userRepresentation = this.getUserRepresentation(client, realmResource, username);
    var roleScope = client.getRoleScopeResource(realmResource, userRepresentation.getId());
    client.addRole(roleScope, roleRepresentation);
  }


  public List<KeycloakUserDto> getRoleUserMembers(KeycloakAdminClient client, String role) {
    var realmResource = client.getRealmResource();
    return client.getRoleUserMembers(realmResource, role).stream()
        .filter(this::hasFullNameAttribute)
        .map(user -> new KeycloakUserDto(user.getUsername(),
            user.getAttributes().get(KeycloakSystemAttribute.FULL_NAME_ATTRIBUTE)
                .get(KeycloakSystemAttribute.FULL_NAME_ATTRIBUTE_INDEX)))
        .sorted(Comparator.comparing(KeycloakUserDto::getFullName))
        .collect(Collectors.toList());
  }

  /**
   * Used for filtering out service account users
   *
   * @param user - keycloak user representation
   * @return true if keycloak user has fullName attribute and false otherwise
   */
  private boolean hasFullNameAttribute(UserRepresentation user) {
    var attribute = user.getAttributes();
    return Objects.nonNull(attribute) && Objects
        .nonNull(attribute.get(KeycloakSystemAttribute.FULL_NAME_ATTRIBUTE));
  }

  private UserRepresentation getUserRepresentation(KeycloakAdminClient client,
      RealmResource realmResource, String userName) {
    var users = client.getUsersRepresentationByUsername(realmResource, userName);

    if (users.size() != 1) {
      throw new KeycloakException(
          String.format("Found %d users with name %s, but expect one", users.size(), userName));
    }
    return users.get(0);
  }
}
