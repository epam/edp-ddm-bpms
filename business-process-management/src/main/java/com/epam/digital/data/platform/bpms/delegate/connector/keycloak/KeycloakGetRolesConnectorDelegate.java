package com.epam.digital.data.platform.bpms.delegate.connector.keycloak;

import com.epam.digital.data.platform.bpms.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.starter.security.dto.enums.KeycloakPlatformRole;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to get the list of
 * regulations roles from keycloak.
 */
@Component(KeycloakGetRolesConnectorDelegate.DELEGATE_NAME)
public class KeycloakGetRolesConnectorDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "keycloakGetRolesConnectorDelegate";
  private static final String ROLES_PARAMETER = "roles";

  @Value("${keycloak.citizen.realm}")
  private String realm;
  @Autowired
  @Qualifier("citizen-keycloak-client")
  private Keycloak keycloak;

  @Override
  public void execute(DelegateExecution execution) {
    var keycloakRoles = keycloak.realm(realm).roles();
    var regulationsRoles = keycloakRoles.list().stream()
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
