package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.KeycloakUserDto;
import com.epam.digital.data.platform.bpms.extension.service.KeycloakClientService;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to get the list of
 * users from keycloak by role.
 */
@Component(KeycloakGetOfficerUsersConnectorDelegate.DELEGATE_NAME)
public class KeycloakGetOfficerUsersConnectorDelegate extends BaseKeycloakOfficerConnectorDelegate {

  public static final String DELEGATE_NAME = "keycloakGetUsersConnectorDelegate";

  private static final String DEFAULT_ROLE = "officer";

  @SystemVariable(name = "role_name")
  private NamedVariableAccessor<String> roleNameVariable;
  @SystemVariable(name = "usersByRole")
  private NamedVariableAccessor<List<KeycloakUserDto>> usersByRoleVariable;

  public KeycloakGetOfficerUsersConnectorDelegate(
      @Qualifier("officer-keycloak-service") KeycloakClientService keycloakClientService) {
    super(keycloakClientService);
  }

  @Override
  public void executeInternal(DelegateExecution execution) throws Exception {
    logStartDelegateExecution();
    var role = roleNameVariable.from(execution).getOrDefault(DEFAULT_ROLE);

    logProcessExecution("get realm resource");
    var realmResource = keycloakClientService.getRealmResource();
    logProcessExecution("get users by role", role);
    var roleUserMembers = keycloakClientService.getRoleUserMembers(realmResource, role);

    usersByRoleVariable.on(execution).set(roleUserMembers);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
