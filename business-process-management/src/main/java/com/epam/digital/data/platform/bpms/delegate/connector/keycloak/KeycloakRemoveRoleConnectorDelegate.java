package com.epam.digital.data.platform.bpms.delegate.connector.keycloak;

import java.util.List;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseKeycloakRoleConnectorDelegate} that is used
 * to remove role from the keycloak user.
 */
@Component(KeycloakRemoveRoleConnectorDelegate.DELEGATE_NAME)
public class KeycloakRemoveRoleConnectorDelegate extends BaseKeycloakRoleConnectorDelegate {

  public static final String DELEGATE_NAME = "keycloakRemoveRoleConnectorDelegate";

  @Override
  protected void performOperationWithRole(RoleScopeResource roleScopeResource,
      List<RoleRepresentation> roleRepresentationList) {
    roleScopeResource.remove(roleRepresentationList);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
