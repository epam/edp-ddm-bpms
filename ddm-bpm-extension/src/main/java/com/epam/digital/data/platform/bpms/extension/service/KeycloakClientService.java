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
import java.util.List;
import org.keycloak.representations.idm.RoleRepresentation;

public interface KeycloakClientService {

  /**
   * Retrieve list of keycloak role representations.
   *
   * @param client keycloak admin client for specific realm
   * @return list of role representations
   */
  List<RoleRepresentation> getKeycloakRoles(KeycloakAdminClient client);

  /**
   * Remove role from keycloak user
   *
   * @param client   keycloak admin client for specific realm
   * @param username user identifier
   * @param role     role identifier to remove
   */
  void removeRole(KeycloakAdminClient client, String username, String role);

  /**
   * Add role to keycloak user
   *
   * @param client   keycloak admin client for specific realm
   * @param username specified user which extends by role
   * @param role     specified role to add
   */
  void addRole(KeycloakAdminClient client, String username, String role);

  /**
   * Retrieve list of keycloak users by realm resource and role name
   *
   * @param client keycloak admin client for specific realm
   * @param role   role name
   * @return list of users
   */
  List<KeycloakUserDto> getRoleUserMembers(KeycloakAdminClient client, String role);

}
