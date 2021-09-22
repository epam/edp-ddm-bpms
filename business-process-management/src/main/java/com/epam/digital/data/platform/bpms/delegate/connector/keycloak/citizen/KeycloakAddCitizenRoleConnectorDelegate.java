package com.epam.digital.data.platform.bpms.delegate.connector.keycloak.citizen;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseKeycloakCitizenRoleConnectorDelegate} that
 * is used to add a new role to the keycloak user.
 */
@Slf4j
@Component(KeycloakAddCitizenRoleConnectorDelegate.DELEGATE_NAME)
public class KeycloakAddCitizenRoleConnectorDelegate extends
    BaseKeycloakCitizenRoleConnectorDelegate {

  public static final String DELEGATE_NAME = "keycloakAddRoleConnectorDelegate";

  @Override
  protected void performOperationWithRole(RoleScopeResource roleScopeResource,
      RoleRepresentation roleRepresentation) {
    var roleName = roleRepresentation.getName();
    log.info("Adding role {} to user", roleName);
    wrapKeycloakVoidRequest(() -> roleScopeResource.add(List.of(roleRepresentation)),
        () -> String.format("Couldn't add role %s to user", roleName));
    log.info("Role {} added to user", roleName);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
