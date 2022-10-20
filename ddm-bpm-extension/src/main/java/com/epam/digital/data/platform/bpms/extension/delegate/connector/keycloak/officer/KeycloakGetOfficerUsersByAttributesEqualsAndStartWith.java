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
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByEqualsAndStartsWithAttributesRequestDto;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component(KeycloakGetOfficerUsersByAttributesEqualsAndStartWith.DELEGATE_NAME)
public class KeycloakGetOfficerUsersByAttributesEqualsAndStartWith extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "keycloakGetOfficerUsersByAttributesEqualsAndStartWith";

  @SystemVariable(name = "attributesEquals")
  private NamedVariableAccessor<Map<String, String>> attributesEquals;
  @SystemVariable(name = "attributesStartWith")
  private NamedVariableAccessor<Map<String, List<String>>> attributesStartWith;
  @SystemVariable(name = "usersByAttribute")
  private NamedVariableAccessor<List<IdmUser>> usersByAttributeVariable;

  @Qualifier("officer-keycloak-client-service")
  private final IdmService idmService;

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Override
  protected void executeInternal(DelegateExecution execution) throws Exception {
    var attributesEqualsVal = attributesEquals.from(execution).get();
    var attributesStartWithVal = attributesStartWith.from(execution).get();

    var searchDto = SearchUsersByEqualsAndStartsWithAttributesRequestDto.builder()
        .attributesStartsWith(Objects.nonNull(attributesStartWithVal) ? attributesStartWithVal
            : Collections.emptyMap())
        .attributesEquals(Objects.nonNull(attributesEqualsVal) ? attributesEqualsVal
            : Collections.emptyMap())
        .build();
    var users = idmService.searchUsers(searchDto);
    usersByAttributeVariable.on(execution).set(users);
  }
}
