package com.epam.digital.data.platform.bpms.delegate.connector.keycloak.citizen;

import com.epam.digital.data.platform.starter.security.dto.enums.KeycloakPlatformRole;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to get the list of
 * regulations roles from keycloak.
 */
@Slf4j
@Component(KeycloakGetCitizenRolesConnectorDelegate.DELEGATE_NAME)
public class KeycloakGetCitizenRolesConnectorDelegate extends BaseKeycloakCitizenConnectorDelegate {

  public static final String DELEGATE_NAME = "keycloakGetRolesConnectorDelegate";
  private static final String ROLES_PARAMETER = "roles";

  @Override
  public void execute(DelegateExecution execution) {
    var realmResource = realmResource();
    var keycloakRoles = getKeycloakRoles(realmResource);
    var regulationsRoles = keycloakRoles.stream()
        .map(RoleRepresentation::getName)
        .filter(Predicate.not(KeycloakPlatformRole::containsRole))
        .collect(Collectors.toList());

    setTransientResult(execution, ROLES_PARAMETER, regulationsRoles);
    logDelegateExecution(execution, Set.of(), Set.of(ROLES_PARAMETER));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  private List<RoleRepresentation> getKeycloakRoles(RealmResource realmResource) {
    log.info("Selecting keycloak roles in realm {}", realmName());
    var keycloakRoles = wrapKeycloakRequest(() -> realmResource.roles().list(),
        () -> String.format("Couldn't select roles from realm %s", realmName()));
    log.info("Founded {} keycloak roles in realm {}", keycloakRoles.size(), realmName());
    return keycloakRoles;
  }
}
