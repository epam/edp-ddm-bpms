package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.citizen;

import com.epam.digital.data.platform.bpms.extension.service.KeycloakClientService;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseKeycloakCitizenRoleConnectorDelegate} that
 * is used to remove role from the keycloak user.
 */
@Component(KeycloakRemoveCitizenRoleConnectorDelegate.DELEGATE_NAME)
public class KeycloakRemoveCitizenRoleConnectorDelegate extends
    BaseKeycloakCitizenRoleConnectorDelegate {

  public static final String DELEGATE_NAME = "keycloakRemoveRoleConnectorDelegate";

  protected KeycloakRemoveCitizenRoleConnectorDelegate(
      @Qualifier("citizen-keycloak-service") KeycloakClientService keycloakClientService) {
    super(keycloakClientService);
  }

  @Override
  protected void performOperationWithRole(RoleScopeResource roleScopeResource,
      RoleRepresentation roleRepresentation) {
    logProcessExecution("remove role", roleRepresentation.getName());
    keycloakClientService.removeRole(roleScopeResource, roleRepresentation);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
