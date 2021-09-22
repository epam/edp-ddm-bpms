package com.epam.digital.data.platform.bpms.delegate.connector.keycloak.citizen;

import com.epam.digital.data.platform.bpms.delegate.connector.keycloak.BaseKeycloakConnectorDelegate;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

public abstract class BaseKeycloakCitizenConnectorDelegate extends
    BaseKeycloakConnectorDelegate {

  @Value("${keycloak.citizen.realm}")
  private String realm;
  @Autowired
  @Qualifier("citizen-keycloak-client")
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
