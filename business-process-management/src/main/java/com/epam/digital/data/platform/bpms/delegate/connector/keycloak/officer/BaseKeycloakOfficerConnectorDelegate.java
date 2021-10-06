package com.epam.digital.data.platform.bpms.delegate.connector.keycloak.officer;

import com.epam.digital.data.platform.bpms.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.service.KeycloakClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class BaseKeycloakOfficerConnectorDelegate extends BaseJavaDelegate {

  @Autowired
  @Qualifier("officer-keycloak-service")
  protected KeycloakClientService keycloakClientService;
}
