package com.epam.digital.data.platform.bpms.delegate.connector.keycloak.officer;

import com.epam.digital.data.platform.bpms.delegate.connector.keycloak.BaseKeycloakConnectorDelegate;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

public abstract class BaseKeycloakOfficerConnectorDelegate extends
    BaseKeycloakConnectorDelegate {

  @Value("${keycloak.officer.realm}")
  private String realm;
  @Autowired
  @Qualifier("officer-keycloak-client")
  private Keycloak keycloak;

  @Override
  protected String realmName() {
    return realm;
  }

  @Override
  protected Keycloak keycloakClient() {
    return keycloak;
  }
}
