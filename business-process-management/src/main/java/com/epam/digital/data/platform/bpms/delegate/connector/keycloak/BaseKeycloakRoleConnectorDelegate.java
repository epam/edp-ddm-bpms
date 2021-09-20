package com.epam.digital.data.platform.bpms.delegate.connector.keycloak;

import com.epam.digital.data.platform.bpms.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.exception.KeycloakNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to provide common
 * logic for working with the keycloak client to perform operations with user role
 */
@Slf4j
public abstract class BaseKeycloakRoleConnectorDelegate extends BaseJavaDelegate {

  private static final String USER_NAME_PARAMETER = "user_name";
  private static final String ROLE_PARAMETER = "role";

  @Value("${keycloak.citizen.realm}")
  private String realm;
  @Autowired
  @Qualifier("citizen-keycloak-client")
  private Keycloak keycloak;

  @Override
  public void execute(DelegateExecution execution) {
    log.debug("Started getting keycloak roles for realm {}", realm);
    var userName = (String) execution.getVariable(USER_NAME_PARAMETER);
    var role = (String) execution.getVariable(ROLE_PARAMETER);

    var userRepresentation = getUserRepresentation(userName);

    var realmResource = keycloak.realm(realm);
    var roleRepresentation = getRoleRepresentation(realmResource, role);
    var userId = userRepresentation.getId();
    var userResource = realmResource.users().get(userId);
    var roleScopeResource = userResource.roles().realmLevel();
    performOperationWithRole(roleScopeResource, Collections.singletonList(roleRepresentation));
    logDelegateExecution(execution, Set.of(USER_NAME_PARAMETER, ROLE_PARAMETER), Set.of());
  }

  protected abstract void performOperationWithRole(RoleScopeResource roleScopeResource,
      List<RoleRepresentation> roleRepresentationList);

  private RoleRepresentation getRoleRepresentation(RealmResource realmResource, String role) {
    try {
      return realmResource.roles().get(role).toRepresentation();
    } catch (Exception exception) {
      throw new KeycloakNotFoundException("Keycloak role not found!");
    }
  }

  private UserRepresentation getUserRepresentation(String userName) {
    log.debug("Finding user {} in keycloak realm {}", userName, realm);
    return keycloak.realm(realm).users().search(userName)
        .stream()
        .filter(user -> userName.equals(user.getUsername()))
        .findFirst()
        .orElseThrow(
            () -> new KeycloakNotFoundException("Keycloak user not found!"));
  }
}
