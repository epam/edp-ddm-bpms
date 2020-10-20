/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.bpms.security.manager.factory;

import com.epam.digital.data.platform.bpms.security.config.CamundaRegistryRoles;
import com.epam.digital.data.platform.bpms.security.manager.DdmAuthorizationManager;
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
