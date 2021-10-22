package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer;

import java.util.Objects;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to get the list of
 * users from keycloak by role.
 */
@Component(KeycloakGetOfficerUsersConnectorDelegate.DELEGATE_NAME)
public class KeycloakGetOfficerUsersConnectorDelegate extends BaseKeycloakOfficerConnectorDelegate {

  public static final String DELEGATE_NAME = "keycloakGetUsersConnectorDelegate";

  private static final String DEFAULT_ROLE = "officer";
  private static final String ROLE_NAME_VAR = "role_name";
  private static final String RESULT_NAME_VAR = "usersByRole";

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    logStartDelegateExecution();
    var role = Objects.requireNonNullElse((String) execution.getVariable(ROLE_NAME_VAR),
        DEFAULT_ROLE);

    logProcessExecution("get realm resource");
    var realmResource = keycloakClientService.getRealmResource();
    logProcessExecution("get users by role", role);
    var roleUserMembers = keycloakClientService.getRoleUserMembers(realmResource, role);

    setResult(execution, RESULT_NAME_VAR, roleUserMembers);
    logDelegateExecution(execution, Set.of(ROLE_NAME_VAR), Set.of(RESULT_NAME_VAR));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
