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

import com.epam.digital.data.platform.starter.security.dto.enums.KeycloakPlatformRole;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.keycloak.representations.idm.RoleRepresentation;

public class UserRoleDelegateUtils {

  public static final Map<String, UnaryOperator<List<RoleRepresentation>>> ROLES_BY_TYPE =
      Map.of(
          "REGISTRY ROLES", registryRoles -> registryRoles.stream()
              .filter(Predicate.not(UserRoleDelegateUtils::isPlatformRole))
              .collect(Collectors.toList()),
          "PLATFORM ROLES", platformRoles -> platformRoles.stream()
              .filter(UserRoleDelegateUtils::isPlatformRole)
              .collect(Collectors.toList()),
          "ALL ROLES", roles -> roles
      );

  private static boolean isPlatformRole(RoleRepresentation role) {
    return KeycloakPlatformRole.containsRole(role.getName());
  }
}
