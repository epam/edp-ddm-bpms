package com.epam.digital.data.platform.bpms.engine.manager.factory;

import com.epam.digital.data.platform.bpms.engine.config.CamundaRegistryRoles;
import com.epam.digital.data.platform.bpms.engine.manager.DdmAuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Authorization manager factory that is used for creating {@link DdmAuthorizationManager} on the
 * place of {@link AuthorizationManager}
 *
 * @see CamundaRegistryRoles
 */
@Component
@Qualifier("authorizationManagerFactory")
public class DdmAuthorizationManagerFactory extends BaseClassManagerFactory<AuthorizationManager> {

  private final CamundaRegistryRoles camundaRegistryRoles;

  public DdmAuthorizationManagerFactory(CamundaRegistryRoles camundaRegistryRoles) {
    super(AuthorizationManager.class);
    this.camundaRegistryRoles = camundaRegistryRoles;
  }

  @Override
  public AuthorizationManager openSession() {
    return new DdmAuthorizationManager(camundaRegistryRoles.getAvailableAuthorizedRoles());
  }
}
