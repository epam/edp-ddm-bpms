package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.citizen;

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
    logStartDelegateExecution();
    var userName = (String) execution.getVariable(USER_NAME_PARAMETER);
    var role = (String) execution.getVariable(ROLE_PARAMETER);

    logProcessExecution("get realm resource");
    var realmResource = keycloakClientService.getRealmResource();
    logProcessExecution("get role representation by name", role);
    var roleRepresentation = keycloakClientService.getRoleRepresentation(realmResource, role);
    logProcessExecution("get user representation by name", userName);
    var userRepresentation = keycloakClientService.getUserRepresentation(realmResource, userName);
    logProcessExecution("get role scope resource by user representation id",
        userRepresentation.getId());
    var roleScopeResource = keycloakClientService
        .getRoleScopeResource(realmResource, userRepresentation.getId());

    performOperationWithRole(roleScopeResource, roleRepresentation);
    logDelegateExecution(execution, Set.of(USER_NAME_PARAMETER, ROLE_PARAMETER), Set.of());
  }

  protected abstract void performOperationWithRole(RoleScopeResource roleScopeResource,
      RoleRepresentation roleRepresentation);
}
