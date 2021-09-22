package com.epam.digital.data.platform.bpms.delegate.connector.keycloak.officer;

import com.epam.digital.data.platform.bpms.delegate.dto.KeycloakUserDto;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to get the list of
 * users from keycloak by role.
 */
@Slf4j
@Component(KeycloakGetOfficerUsersConnectorDelegate.DELEGATE_NAME)
public class KeycloakGetOfficerUsersConnectorDelegate extends BaseKeycloakOfficerConnectorDelegate {

  public static final String DELEGATE_NAME = "keycloakGetUsersConnectorDelegate";

  private static final String DEFAULT_ROLE = "officer";
  private static final String ROLE_NAME_VAR = "role_name";
  private static final String RESULT_NAME_VAR = "usersByRole";
  private static final String ATTRIBUTE_NAME = "fullName";
  private static final int ATTRIBUTE_INDEX = 0;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    var role = Objects.requireNonNullElse((String) execution.getVariable(ROLE_NAME_VAR),
        DEFAULT_ROLE);

    var roleUserMembers = getRoleUserMembers(role);

    var users = roleUserMembers.stream()
        .filter(this::hasFullNameAttribute)
        .map(user -> new KeycloakUserDto(user.getUsername(),
            user.getAttributes().get(ATTRIBUTE_NAME).get(ATTRIBUTE_INDEX)))
        .sorted(Comparator.comparing(KeycloakUserDto::getFullName))
        .collect(Collectors.toList());

    setResult(execution, RESULT_NAME_VAR, users);
    logDelegateExecution(execution, Set.of(ROLE_NAME_VAR), Set.of(RESULT_NAME_VAR));
  }

  private Set<UserRepresentation> getRoleUserMembers(String role) {
    log.info("Selecting keycloak users with role {} in realm {}", role, realmName());
    var result = wrapKeycloakRequest(() -> realmResource().roles().get(role).getRoleUserMembers(),
        () -> String.format("Couldn't get keycloak users with role %s in realm %s", role,
            realmName()));
    log.info("Selected {} users with role {} in realm {}", result.size(), role, realmName());
    return result;
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  /**
   * Used for filtering out service account users
   *
   * @param user - keycloak user representation
   * @return true if keycloak user has fullName attribute and false otherwise
   */
  private boolean hasFullNameAttribute(UserRepresentation user) {
    var attribute = user.getAttributes();
    return Objects.nonNull(attribute) && Objects.nonNull(attribute.get(ATTRIBUTE_NAME));
  }
}
