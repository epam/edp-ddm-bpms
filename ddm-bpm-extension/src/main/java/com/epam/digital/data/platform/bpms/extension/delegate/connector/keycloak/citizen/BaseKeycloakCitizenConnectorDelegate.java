package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.citizen;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.service.KeycloakClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class BaseKeycloakCitizenConnectorDelegate extends BaseJavaDelegate {

  @Autowired
  @Qualifier("citizen-keycloak-service")
  protected KeycloakClientService keycloakClientService;
}
