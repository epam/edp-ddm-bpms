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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to get the list of
 * users from keycloak by role.
 */
@RequiredArgsConstructor
@Component(KeycloakGetOfficerUsersByAttributesConnectorDelegate.DELEGATE_NAME)
public class KeycloakGetOfficerUsersByAttributesConnectorDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "keycloakGetOfficerUsersByAttributesConnectorDelegate";

  @Qualifier("officer-keycloak-client-service")
  private final IdmService idmService;

  @SystemVariable(name = "edrpou")
  private NamedVariableAccessor<String> edrpouVariable;
  @SystemVariable(name = "drfo")
  private NamedVariableAccessor<String> drfoVariable;
  @SystemVariable(name = "usersByAttribute")
  private NamedVariableAccessor<List<String>> usersByAttributeVariable;

  @Override
  public void executeInternal(@NonNull DelegateExecution execution) throws Exception {
    var edrpou = getRequiredEdrpou(execution);
    var drfo = drfoVariable.from(execution).getOptional();

    var searchUsersQueryBuilder = SearchUserQuery.builder()
        .edrpou(edrpou);
    if (drfo.isPresent() && !drfo.get().isEmpty()) {
      searchUsersQueryBuilder.drfo(drfo.get());
    }

    var usernames = idmService.searchUsers(searchUsersQueryBuilder.build())
        .stream()
        .map(IdmUser::getUserName)
        .collect(Collectors.toList());

    usersByAttributeVariable.on(execution).set(usernames);
  }

  @NonNull
  private String getRequiredEdrpou(@NonNull DelegateExecution execution) {
    var edrpou = edrpouVariable.from(execution).getOptional();
    if (edrpou.isEmpty() || edrpou.get().isEmpty()) {
      throw new IllegalArgumentException(
          String.format("Edrpou wasn't specified for %s delegate in process with id %s",
              DELEGATE_NAME, execution.getProcessDefinitionId()));
    }
    return edrpou.get();
  }

  @NonNull
  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
