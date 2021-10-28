package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.citizen;

import com.epam.digital.data.platform.bpms.extension.service.KeycloakClientService;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
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

  @SystemVariable(name = "user_name")
  private NamedVariableAccessor<String> userNameVariable;
  @SystemVariable(name = "role")
  private NamedVariableAccessor<String> roleVariable;

  protected BaseKeycloakCitizenRoleConnectorDelegate(KeycloakClientService keycloakClientService) {
    super(keycloakClientService);
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    logStartDelegateExecution();
    var userName = userNameVariable.from(execution).get();
    var role = roleVariable.from(execution).get();

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
  }

  protected abstract void performOperationWithRole(RoleScopeResource roleScopeResource,
      RoleRepresentation roleRepresentation);
}
