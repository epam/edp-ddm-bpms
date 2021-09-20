package com.epam.digital.data.platform.bpms.delegate.connector.keycloak;

import com.epam.digital.data.platform.bpms.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.delegate.dto.KeycloakUserDto;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to get the list of
 * users from keycloak by role.
 */
@Component(KeycloakGetUsersConnectorDelegate.DELEGATE_NAME)
public class KeycloakGetUsersConnectorDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "keycloakGetUsersConnectorDelegate";

  private static final String DEFAULT_ROLE = "officer";
  private static final String ROLE_NAME_VAR = "role_name";
  private static final String RESULT_NAME_VAR = "usersByRole";
  private static final String ATTRIBUTE_NAME = "fullName";
  private static final int ATTRIBUTE_INDEX = 0;

  @Value("${keycloak.officer.realm}")
  private String realm;
  @Autowired
  @Qualifier("officer-keycloak-client")
  private Keycloak keycloak;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    var role = (String) execution.getVariable(ROLE_NAME_VAR);

    var roleUserMembers = keycloak.realm(realm).roles()
        .get(Objects.requireNonNullElse(role, DEFAULT_ROLE))
        .getRoleUserMembers();

    var usersMap = roleUserMembers.stream()
        .filter(this::hasFullNameAttribute)
        .collect(Collectors.toMap(UserRepresentation::getUsername,
            user -> user.getAttributes().get(ATTRIBUTE_NAME).get(ATTRIBUTE_INDEX)));

    var usersByRole = usersMap.entrySet().stream()
        .map(entry -> new KeycloakUserDto(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());

    setResult(execution, RESULT_NAME_VAR, usersByRole);
    logDelegateExecution(execution, Set.of(ROLE_NAME_VAR), Set.of(RESULT_NAME_VAR));
  }

  private boolean hasFullNameAttribute(UserRepresentation user) {
    var attribute = user.getAttributes();
    return Objects.nonNull(attribute) && Objects.nonNull(attribute.get(ATTRIBUTE_NAME));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
