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

package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.citizen;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is
 * used to add a new role to the keycloak user.
 */
@RequiredArgsConstructor
@Component(KeycloakAddCitizenRoleConnectorDelegate.DELEGATE_NAME)
public class KeycloakAddCitizenRoleConnectorDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "keycloakAddRoleConnectorDelegate";

  @Qualifier("citizen-keycloak-client-service")
  private  final  IdmService idmService;

  @SystemVariable(name = "user_name")
  protected NamedVariableAccessor<String> userNameVariable;
  @SystemVariable(name = "role")
  protected NamedVariableAccessor<String> roleVariable;

  @Override
  public void executeInternal(DelegateExecution execution) {
    var username = userNameVariable.from(execution).get();
    var role = roleVariable.from(execution).get();

    idmService.addRole(username, role);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
