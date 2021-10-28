package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.citizen;

import com.epam.digital.data.platform.bpms.extension.service.KeycloakClientService;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.starter.security.dto.enums.KeycloakPlatformRole;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to get the list of
 * regulations roles from keycloak.
 */
@Component(KeycloakGetCitizenRolesConnectorDelegate.DELEGATE_NAME)
public class KeycloakGetCitizenRolesConnectorDelegate extends BaseKeycloakCitizenConnectorDelegate {

  public static final String DELEGATE_NAME = "keycloakGetRolesConnectorDelegate";

  @SystemVariable(name = "roles", isTransient = true)
  private NamedVariableAccessor<List<String>> rolesVariable;

  public KeycloakGetCitizenRolesConnectorDelegate(
      @Qualifier("citizen-keycloak-service") KeycloakClientService keycloakClientService) {
    super(keycloakClientService);
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    logStartDelegateExecution();
    logProcessExecution("get realm resource");
    var realmResource = keycloakClientService.getRealmResource();
    logProcessExecution("get keycloak roles");
    var keycloakRoles = keycloakClientService.getKeycloakRoles(realmResource);
    logProcessExecution("keycloak role filtering");
    var regulationsRoles = keycloakRoles.stream()
        .map(RoleRepresentation::getName)
        .filter(Predicate.not(KeycloakPlatformRole::containsRole))
        .collect(Collectors.toList());
    rolesVariable.on(execution).set(regulationsRoles);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
