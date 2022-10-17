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
import com.epam.digital.data.platform.integration.idm.model.SearchUserQuery;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component(KeycloakGetExtendedOfficerUsersByAttributesConnectorDelegate.DELEGATE_NAME)
public class KeycloakGetExtendedOfficerUsersByAttributesConnectorDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "keycloakGetExtendedOfficerUsersByAttributesConnectorDelegate";

  @Qualifier("officer-keycloak-client-service")
  private final IdmService idmService;

  @SystemVariable(name = "attributes")
  private NamedVariableAccessor<Map<String, String>> attributes;
  @SystemVariable(name = "usersByAttribute")
  private NamedVariableAccessor<List<IdmUser>> usersByAttributeVariable;

  @Override
  public void executeInternal(@NonNull DelegateExecution execution) throws Exception {
    var searchUsersQuery = SearchUserQuery.builder()
        .attributes(attributes.from(execution).get()).build();

    var users = idmService.searchUsers(searchUsersQuery);

    usersByAttributeVariable.on(execution).set(users);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
