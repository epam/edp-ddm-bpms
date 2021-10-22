package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.citizen;

import com.epam.digital.data.platform.starter.security.dto.enums.KeycloakPlatformRole;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to get the list of
 * regulations roles from keycloak.
 */
@Component(KeycloakGetCitizenRolesConnectorDelegate.DELEGATE_NAME)
public class KeycloakGetCitizenRolesConnectorDelegate extends BaseKeycloakCitizenConnectorDelegate {

  public static final String DELEGATE_NAME = "keycloakGetRolesConnectorDelegate";
  private static final String ROLES_PARAMETER = "roles";

  @Override
  public void execute(DelegateExecution execution) {
    logStartDelegateExecution();
    logProcessExecution("get realm resource");
    var realmResource = keycloakClientService.getRealmResource();
    logProcessExecution("get keycloak roles");
    var keycloakRoles = keycloakClientService.getKeycloakRoles(realmResource);
    logProcessExecution("keycloak role filtering");
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
}
