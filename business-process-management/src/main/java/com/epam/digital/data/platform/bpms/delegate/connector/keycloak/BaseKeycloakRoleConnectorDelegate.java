package com.epam.digital.data.platform.bpms.delegate.connector.keycloak;

import com.epam.digital.data.platform.bpms.exception.KeycloakNotFoundException;
import java.util.Collections;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to provide common
 * logic for working with the keycloak client to perform operations with user role
 */
public abstract class BaseKeycloakRoleConnectorDelegate implements JavaDelegate {

  @Value("${keycloak.url}")
  private String serverUrl;
  @Value("${keycloak.realm}")
  private String realm;
  @Value("${keycloak.client-id}")
  private String clientId;
  @Value("${keycloak.client-secret}")
  private String clientSecret;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    var userName = (String) execution.getVariable("user_name");
    var role = (String) execution.getVariable("role");

    var keycloak = KeycloakBuilder.builder()
        .serverUrl(serverUrl)
        .realm(realm)
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId(clientId)
        .clientSecret(clientSecret)
        .build();

    var userRepresentation = getUserRepresentation(keycloak, userName);

    var realmResource = keycloak.realm(realm);
    var roleRepresentation = getRoleRepresentation(realmResource, role);
    var userId = userRepresentation.getId();
    var userResource = realmResource.users().get(userId);
    var roleScopeResource = userResource.roles().realmLevel();
    performOperationWithRole(roleScopeResource, Collections.singletonList(roleRepresentation));
  }

  protected abstract void performOperationWithRole(RoleScopeResource roleScopeResource,
      List<RoleRepresentation> roleRepresentationList);

  private RoleRepresentation getRoleRepresentation(RealmResource realmResource, String role) {
    try {
      return realmResource.roles().get(role).toRepresentation();
    } catch (Exception exception) {
      throw new KeycloakNotFoundException("Keycloak role not found!");
    }
  }

  private UserRepresentation getUserRepresentation(Keycloak keycloak, String userName) {
    var userRepresentation = keycloak.realm(realm).users().search(userName)
        .stream()
        .filter(user -> userName.equals(user.getUsername()))
        .findFirst();
    if (userRepresentation.isPresent()) {
      return userRepresentation.get();
    } else {
      throw new KeycloakNotFoundException("Keycloak user not found!");
    }
  }
}
