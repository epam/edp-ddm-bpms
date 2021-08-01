package com.epam.digital.data.platform.bpms.delegate.connector.keycloak;

import com.epam.digital.data.platform.starter.logger.annotation.Logging;
import java.util.List;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseKeycloakRoleConnectorDelegate} that is used to
 * add a new role to the keycloak user.
 */
@Component("keycloakAddRoleConnectorDelegate")
@Logging
public class KeycloakAddRoleConnectorDelegate extends BaseKeycloakRoleConnectorDelegate {

  @Override
  protected void performOperationWithRole(RoleScopeResource roleScopeResource,
      List<RoleRepresentation> roleRepresentationList) {
    roleScopeResource.add(roleRepresentationList);
  }
}
