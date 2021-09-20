package com.epam.digital.data.platform.bpms.delegate.connector.keycloak;

import java.util.List;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseKeycloakRoleConnectorDelegate} that is used
 * to add a new role to the keycloak user.
 */
@Component(KeycloakAddRoleConnectorDelegate.DELEGATE_NAME)
public class KeycloakAddRoleConnectorDelegate extends BaseKeycloakRoleConnectorDelegate {

  public static final String DELEGATE_NAME = "keycloakAddRoleConnectorDelegate";

  @Override
  protected void performOperationWithRole(RoleScopeResource roleScopeResource,
      List<RoleRepresentation> roleRepresentationList) {
    roleScopeResource.add(roleRepresentationList);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
