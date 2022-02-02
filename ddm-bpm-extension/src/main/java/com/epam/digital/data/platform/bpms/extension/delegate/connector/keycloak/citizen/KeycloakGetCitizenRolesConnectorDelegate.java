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
import com.epam.digital.data.platform.integration.idm.client.KeycloakAdminClient;
import com.epam.digital.data.platform.starter.security.dto.enums.KeycloakPlatformRole;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to get the list of
 * regulations roles from keycloak.
 */
@Slf4j
@Component(KeycloakGetCitizenRolesConnectorDelegate.DELEGATE_NAME)
public class KeycloakGetCitizenRolesConnectorDelegate extends BaseKeycloakCitizenConnectorDelegate {

  public static final String DELEGATE_NAME = "keycloakGetRolesConnectorDelegate";

  @SystemVariable(name = "roles", isTransient = true)
  private NamedVariableAccessor<List<String>> rolesVariable;

  public KeycloakGetCitizenRolesConnectorDelegate(
      @Qualifier("citizen-keycloak-admin-client")KeycloakAdminClient citizenKeycloakAdminClient,
      KeycloakClientService keycloakClientService) {
    super(citizenKeycloakAdminClient, keycloakClientService);
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var keycloakRoles = keycloakClientService.getKeycloakRoles(citizenKeycloakAdminClient);
    log.debug("Start filtering keycloak roles {}", keycloakRoles);
    var regulationsRoles = keycloakRoles.stream()
        .map(RoleRepresentation::getName)
        .filter(Predicate.not(KeycloakPlatformRole::containsRole))
        .collect(Collectors.toList());
    log.debug("Keycloak roles {} was filtered", regulationsRoles);
    rolesVariable.on(execution).set(regulationsRoles);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
