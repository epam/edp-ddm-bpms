package com.epam.digital.data.platform.bpms.delegate.connector.keycloak.citizen;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseKeycloakCitizenRoleConnectorDelegate} that
 * is used to remove role from the keycloak user.
 */
@Slf4j
@Component(KeycloakRemoveCitizenRoleConnectorDelegate.DELEGATE_NAME)
public class KeycloakRemoveCitizenRoleConnectorDelegate extends
    BaseKeycloakCitizenRoleConnectorDelegate {

  public static final String DELEGATE_NAME = "keycloakRemoveRoleConnectorDelegate";

  @Override
  protected void performOperationWithRole(RoleScopeResource roleScopeResource,
      RoleRepresentation roleRepresentation) {
    var roleName = roleRepresentation.getName();
    log.info("Removing role {} from user", roleName);
    wrapKeycloakVoidRequest(() -> roleScopeResource.remove(List.of(roleRepresentation)),
        () -> String.format("Couldn't remove role %s from user", roleName));
    log.info("Role {} removed from user", roleName);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
