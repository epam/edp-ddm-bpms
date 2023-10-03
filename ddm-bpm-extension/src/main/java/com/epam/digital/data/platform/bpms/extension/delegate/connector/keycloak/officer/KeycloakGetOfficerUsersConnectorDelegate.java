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
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to get the list of
 * users from keycloak by role.
 */
@RequiredArgsConstructor
@Component(KeycloakGetOfficerUsersConnectorDelegate.DELEGATE_NAME)
public class KeycloakGetOfficerUsersConnectorDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "keycloakGetUsersConnectorDelegate";
  private static final String DEFAULT_ROLE = "officer";
  private static final Integer DEFAULT_OFFSET= 0;
  private static final Integer DEFAULT_LIMIT = 100;

  @Qualifier("officer-keycloak-client-service")
  private final IdmService idmService;

  @SystemVariable(name = "role_name")
  private NamedVariableAccessor<String> roleNameVariable;
  @SystemVariable(name = "usersByRole")
  private NamedVariableAccessor<List<IdmUser>> usersByRoleVariable;
  @SystemVariable(name = "limit")
  private NamedVariableAccessor<String> limitVariable;
  @SystemVariable(name = "offset")
  private NamedVariableAccessor<String> offsetVariable;

  @Override
  public void executeInternal(DelegateExecution execution) throws Exception {
    usersByRoleVariable.on(execution).set(List.of());

    var role = roleNameVariable.from(execution).getOrDefault(DEFAULT_ROLE);
    var offset = toInteger(offsetVariable.from(execution).get());
    var limit = toInteger(limitVariable.from(execution).get());

    var roleUserMembers = idmService.getRoleUserMembers(role,
        Objects.requireNonNullElse(offset, DEFAULT_OFFSET),
        Objects.requireNonNullElse(limit, DEFAULT_LIMIT));
    usersByRoleVariable.on(execution).set(roleUserMembers);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Nullable
  private Integer toInteger(@Nullable String value) {
    return Objects.isNull(value) ? null : Integer.valueOf(value);
  }
}
