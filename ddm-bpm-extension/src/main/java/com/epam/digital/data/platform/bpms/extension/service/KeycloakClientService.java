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
import java.util.List;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cloud.sleuth.annotation.NewSpan;

public interface KeycloakClientService {

  /**
   * Retrieve keycloak realm resource.
   *
   * @return realm resource
   */
  @NewSpan
  RealmResource getRealmResource();

  /**
   * Retrieve list of keycloak role representations by realm resource
   *
   * @param realmResource realm resource
   * @return list of role representations
   */
  @NewSpan
  List<RoleRepresentation> getKeycloakRoles(RealmResource realmResource);

  /**
   * Retrieve keycloak user representation by realm resource and user name
   *
   * @param realmResource realm resource
   * @param userName      user name
   * @return user representation
   */
  @NewSpan
  UserRepresentation getUserRepresentation(RealmResource realmResource, String userName);

  /**
   * Retrieve keycloak role representation by realm resource and role name
   *
   * @param realmResource realm resource
   * @param role          role name
   * @return role representation
   */
  @NewSpan
  RoleRepresentation getRoleRepresentation(RealmResource realmResource, String role);

  /**
   * Retrieve list of keycloak users by realm resource and role name
   *
   * @param realmResource realm resource
   * @param role          role name
   * @return list of users
   */
  @NewSpan
  List<KeycloakUserDto> getRoleUserMembers(RealmResource realmResource, String role);

  /**
   * Retrieve keycloak role scope resource by realm resource and user id
   *
   * @param realmResource realm resource
   * @param userId        user identifier
   * @return role scope resource
   */
  @NewSpan
  RoleScopeResource getRoleScopeResource(RealmResource realmResource, String userId);

  /**
   * Add role to keycloak user
   *
   * @param roleScopeResource  role scope resource
   * @param roleRepresentation role representation to add
   */
  @NewSpan
  void removeRole(RoleScopeResource roleScopeResource, RoleRepresentation roleRepresentation);

  /**
   * Remove role from keycloak user
   *
   * @param roleScopeResource  role scope resource
   * @param roleRepresentation role representation to remove
   */
  @NewSpan
  void addRole(RoleScopeResource roleScopeResource, RoleRepresentation roleRepresentation);
}
