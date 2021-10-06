package com.epam.digital.data.platform.bpms.service;

import com.epam.digital.data.platform.bpms.delegate.dto.KeycloakUserDto;
import com.epam.digital.data.platform.bpms.exception.KeycloakException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

@Slf4j
@RequiredArgsConstructor
public class KeycloakClientServiceImpl implements KeycloakClientService {

  private static final String FULL_NAME_ATTRIBUTE = "fullName";
  private static final int FULL_NAME_ATTRIBUTE_INDEX = 0;

  private final String realmName;
  private final Keycloak keycloak;

  @Override
  public RealmResource getRealmResource() {
    log.info("Selecting keycloak realm {}", realmName);
    var result = wrapKeycloakRequest(() -> keycloak.realm(realmName),
        () -> String.format("Couldn't find realm %s", realmName));
    log.info("Keycloak realm {} found", realmName);
    return result;
  }

  @Override
  public List<RoleRepresentation> getKeycloakRoles(RealmResource realmResource) {
    log.info("Selecting keycloak roles in realm {}", realmName);
    var keycloakRoles = wrapKeycloakRequest(() -> realmResource.roles().list(),
        () -> String.format("Couldn't select roles from realm %s", realmName));
    log.info("Founded {} keycloak roles in realm {}", keycloakRoles.size(), realmName);
    return keycloakRoles;
  }

  @Override
  public UserRepresentation getUserRepresentation(RealmResource realmResource, String userName) {
    log.info("Finding user {} in keycloak realm {}", userName, realmName);
    var users = wrapKeycloakRequest(() -> realmResource.users().search(userName, true),
        () -> String.format("Couldn't find user %s in realm %s", userName, realmName));

    if (users.size() != 1) {
      throw new KeycloakException(
          String.format("Found %d users with name %s in realm %s, but expect one",
              users.size(), userName, realmName));
    }
    log.info("User {} in realm {} found", userName, realmName);
    return users.get(0);
  }

  @Override
  public RoleRepresentation getRoleRepresentation(RealmResource realmResource, String role) {
    log.info("Finding role {} in keycloak realm {}", role, realmName);
    var result = wrapKeycloakRequest(() -> realmResource.roles().get(role).toRepresentation(),
        () -> String.format("Couldn't find role %s in realm %s", role, realmName));
    log.info("Role {} in realm {} is found", role, realmName);
    return result;
  }

  @Override
  public List<KeycloakUserDto> getRoleUserMembers(RealmResource realmResource, String role) {
    log.info("Selecting keycloak users with role {} in realm {}", role, realmName);
    var roleUserMembers = wrapKeycloakRequest(() -> realmResource.roles().get(role).getRoleUserMembers(),
        () -> String.format("Couldn't get keycloak users with role %s in realm %s", role,
            realmName));

    var users = roleUserMembers.stream()
        .filter(this::hasFullNameAttribute)
        .map(user -> new KeycloakUserDto(user.getUsername(),
            user.getAttributes().get(FULL_NAME_ATTRIBUTE)
                .get(FULL_NAME_ATTRIBUTE_INDEX)))
        .sorted(Comparator.comparing(KeycloakUserDto::getFullName))
        .collect(Collectors.toList());
    log.info("Selected {} users with role {} in realm {}", users.size(), role, realmName);
    return users;
  }

  @Override
  public RoleScopeResource getRoleScopeResource(RealmResource realmResource, String userId) {
    log.info("Finding keycloak role scope resource by userId {} in realm {}", userId, realmName);
    var result = wrapKeycloakRequest(() -> realmResource.users().get(userId).roles().realmLevel(),
        () -> String
            .format("Couldn't find keycloak role scope resource by userId %s in realm %s", userId,
                realmName));
    log.info("Found role scope resource by userId {} in realm {}", userId, realmName);
    return result;
  }

  @Override
  public void removeRole(RoleScopeResource roleScope, RoleRepresentation roleRepresentation) {
    var roleName = roleRepresentation.getName();
    log.info("Removing role {} from user", roleName);
    wrapKeycloakVoidRequest(() -> roleScope.remove(List.of(roleRepresentation)),
        () -> String.format("Couldn't remove role %s from user", roleName));
    log.info("Role {} removed from user", roleName);
  }

  @Override
  public void addRole(RoleScopeResource roleScope, RoleRepresentation roleRepresentation) {
    var roleName = roleRepresentation.getName();
    log.info("Adding role {} to user", roleName);
    wrapKeycloakVoidRequest(() -> roleScope.add(List.of(roleRepresentation)),
        () -> String.format("Couldn't add role %s to user", roleName));
    log.info("Role {} added to user", roleName);
  }

  /**
   * Used for filtering out service account users
   *
   * @param user - keycloak user representation
   * @return true if keycloak user has fullName attribute and false otherwise
   */
  private boolean hasFullNameAttribute(UserRepresentation user) {
    var attribute = user.getAttributes();
    return Objects.nonNull(attribute) && Objects
        .nonNull(attribute.get(FULL_NAME_ATTRIBUTE));
  }

  private <T> T wrapKeycloakRequest(Supplier<T> supplier, Supplier<String> failMessageSupplier) {
    try {
      return supplier.get();
    } catch (RuntimeException exception) {
      throw new KeycloakException(failMessageSupplier.get(), exception);
    }
  }

  private void wrapKeycloakVoidRequest(Runnable runnable, Supplier<String> failMessageSupplier) {
    try {
      runnable.run();
    } catch (RuntimeException exception) {
      throw new KeycloakException(failMessageSupplier.get(), exception);
    }
  }
}
