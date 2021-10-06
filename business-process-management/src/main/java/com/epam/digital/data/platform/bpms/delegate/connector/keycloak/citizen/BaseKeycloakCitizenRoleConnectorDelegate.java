package com.epam.digital.data.platform.bpms.delegate.connector.keycloak.citizen;

import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to provide common
 * logic for working with the keycloak client to perform operations with user role
 */
public abstract class BaseKeycloakCitizenRoleConnectorDelegate extends
    BaseKeycloakCitizenConnectorDelegate {

  private static final String USER_NAME_PARAMETER = "user_name";
  private static final String ROLE_PARAMETER = "role";

  @Override
  public void execute(DelegateExecution execution) {
    var userName = (String) execution.getVariable(USER_NAME_PARAMETER);
    var role = (String) execution.getVariable(ROLE_PARAMETER);

    var realmResource = keycloakClientService.getRealmResource();
    var roleRepresentation = keycloakClientService.getRoleRepresentation(realmResource, role);
    var userRepresentation = keycloakClientService.getUserRepresentation(realmResource, userName);
    var roleScopeResource = keycloakClientService
        .getRoleScopeResource(realmResource, userRepresentation.getId());

    performOperationWithRole(roleScopeResource, roleRepresentation);
    logDelegateExecution(execution, Set.of(USER_NAME_PARAMETER, ROLE_PARAMETER), Set.of());
  }

  protected abstract void performOperationWithRole(RoleScopeResource roleScopeResource,
      RoleRepresentation roleRepresentation);
}
