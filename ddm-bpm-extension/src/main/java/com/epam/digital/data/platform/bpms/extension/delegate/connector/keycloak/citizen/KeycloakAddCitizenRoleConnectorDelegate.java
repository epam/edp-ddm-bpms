package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.citizen;

import com.epam.digital.data.platform.bpms.extension.service.KeycloakClientService;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseKeycloakCitizenRoleConnectorDelegate} that
 * is used to add a new role to the keycloak user.
 */
@Component(KeycloakAddCitizenRoleConnectorDelegate.DELEGATE_NAME)
public class KeycloakAddCitizenRoleConnectorDelegate extends
    BaseKeycloakCitizenRoleConnectorDelegate {

  public static final String DELEGATE_NAME = "keycloakAddRoleConnectorDelegate";

  protected KeycloakAddCitizenRoleConnectorDelegate(
      @Qualifier("citizen-keycloak-service") KeycloakClientService keycloakClientService) {
    super(keycloakClientService);
  }

  @Override
  protected void performOperationWithRole(RoleScopeResource roleScopeResource,
      RoleRepresentation roleRepresentation) {
    logProcessExecution("add role", roleRepresentation.getName());
    keycloakClientService.addRole(roleScopeResource, roleRepresentation);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
