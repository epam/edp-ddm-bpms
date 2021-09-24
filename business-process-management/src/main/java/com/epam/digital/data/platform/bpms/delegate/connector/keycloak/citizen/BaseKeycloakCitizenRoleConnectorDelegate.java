package com.epam.digital.data.platform.bpms.delegate.connector.keycloak.citizen;

import com.epam.digital.data.platform.bpms.exception.KeycloakException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to provide common
 * logic for working with the keycloak client to perform operations with user role
 */
@Slf4j
public abstract class BaseKeycloakCitizenRoleConnectorDelegate extends
    BaseKeycloakCitizenConnectorDelegate {

  private static final String USER_NAME_PARAMETER = "user_name";
  private static final String ROLE_PARAMETER = "role";

  @Override
  public void execute(DelegateExecution execution) {
    var userName = (String) execution.getVariable(USER_NAME_PARAMETER);
    var role = (String) execution.getVariable(ROLE_PARAMETER);

    var realmResource = realmResource();
    var roleRepresentation = getRoleRepresentation(realmResource, role);

    var userRepresentation = getUserRepresentation(realmResource, userName);
    var userId = userRepresentation.getId();
    var roleScopeResource = realmResource.users().get(userId).roles().realmLevel();

    performOperationWithRole(roleScopeResource, roleRepresentation);
    logDelegateExecution(execution, Set.of(USER_NAME_PARAMETER, ROLE_PARAMETER), Set.of());
  }

  protected abstract void performOperationWithRole(RoleScopeResource roleScopeResource,
      RoleRepresentation roleRepresentation);

  private RoleRepresentation getRoleRepresentation(RealmResource realmResource, String role) {
    log.info("Finding role {} in keycloak realm {}", role, realmName());
    var result = wrapKeycloakRequest(() -> realmResource.roles().get(role).toRepresentation(),
        () -> String.format("Couldn't find role %s in realm %s", role, realmName()));
    log.info("Role {} in realm {} is found", role, realmName());
    return result;
  }

  private UserRepresentation getUserRepresentation(RealmResource realmResource, String userName) {
    log.info("Finding user {} in keycloak realm {}", userName, realmName());
    var users = wrapKeycloakRequest(() -> realmResource.users().search(userName, true),
        () -> String.format("Couldn't find user %s in realm %s", userName, realmName()));

    if (users.size() != 1) {
      throw new KeycloakException(
          String.format("Found %d users with name %s in realm %s, but expect one",
              users.size(), userName, realmName()));
    }

    log.info("User {} in realm {} found", userName, realmName());
    return users.get(0);
  }
}
