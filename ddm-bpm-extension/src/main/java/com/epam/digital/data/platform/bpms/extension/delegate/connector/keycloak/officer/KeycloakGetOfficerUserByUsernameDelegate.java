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

package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.integration.idm.model.IdmUser;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component(KeycloakGetOfficerUserByUsernameDelegate.DELEGATE_NAME)
public class KeycloakGetOfficerUserByUsernameDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "keycloakGetOfficerUserByUsernameDelegate";

  @Qualifier("officer-keycloak-client-service")
  private final IdmService idmService;

  @SystemVariable(name = "username")
  private NamedVariableAccessor<String> username;
  @SystemVariable(name = "userByUsername")
  private NamedVariableAccessor<IdmUser> user;

  @Override
  protected void executeInternal(DelegateExecution execution) throws Exception {
    user.on(execution).set(IdmUser.builder().build());

    var userByUserName = idmService.getUserByUserName(username.from(execution).get());

    user.on(execution).set(userByUserName.get(0));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
