package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.citizen;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.service.KeycloakClientService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseKeycloakCitizenConnectorDelegate extends BaseJavaDelegate {

  protected final KeycloakClientService keycloakClientService;
}
