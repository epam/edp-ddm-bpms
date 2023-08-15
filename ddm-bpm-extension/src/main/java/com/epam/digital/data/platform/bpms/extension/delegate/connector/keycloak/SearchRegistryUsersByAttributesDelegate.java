/*
 * Copyright 2023 EPAM Systems.
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

package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.integration.idm.model.IdmUsersResponse;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByAttributesRequestDto;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByAttributesRequestDto.Pagination;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component(SearchRegistryUsersByAttributesDelegate.DELEGATE_NAME)
public class SearchRegistryUsersByAttributesDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "searchRegistryUsersByAttributes";

  private static final String OFFICER_REALM = "officer";
  private static final String CITIZEN_REALM = "citizen";

  @SystemVariable(name = "realm")
  private NamedVariableAccessor<String> realmVariable;
  @SystemVariable(name = "attributesEquals")
  private NamedVariableAccessor<Map<String, Object>> attributesEqualsVariable;
  @SystemVariable(name = "attributesStartWith")
  private NamedVariableAccessor<Map<String, Object>> attributesStartWithVariable;
  /**
   * It's a former Inverse Start With. When attributesStartWith works like
   * {@code attributeInKeycloak.startsWith(inputAttribute)} this works in reverse
   * {@code inputAttribute.startsWith(attributeInKeycloak)}
   */
  @SystemVariable(name = "attributesThatAreStartFor")
  private NamedVariableAccessor<Map<String, Object>> attributesThatAreStartForVariable;
  @SystemVariable(name = "limit")
  private NamedVariableAccessor<String> limitVariable;
  @SystemVariable(name = "continueToken")
  private NamedVariableAccessor<String> continueTokenVariable;

  @SystemVariable(name = "usersResponse", isTransient = true)
  private NamedVariableAccessor<IdmUsersResponse> usersResponseVariable;

  @Qualifier("officer-keycloak-client-service")
  private final IdmService officerIdmService;
  @Qualifier("citizen-keycloak-client-service")
  private final IdmService citizenIdmService;

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Override
  protected void executeInternal(DelegateExecution execution) throws Exception {
    usersResponseVariable.on(execution).set(IdmUsersResponse.builder().build());

    var realm = realmVariable.from(execution).getOrThrow();
    var idmService = getIdmService(realm);

    var attributesEquals = toMultivaluedMap(attributesEqualsVariable.from(execution).get());
    var attributesStartWith = toMultivaluedMap(attributesStartWithVariable.from(execution).get());
    var attributesThatAreStartFor = toMultivaluedMap(
        attributesThatAreStartForVariable.from(execution).get());
    var limit = toInteger(limitVariable.from(execution).get());
    var continueToken = toInteger(continueTokenVariable.from(execution).get());

    var searchDto = SearchUsersByAttributesRequestDto.builder()
        .attributesEquals(attributesEquals)
        .attributesStartsWith(attributesStartWith)
        .attributesThatAreStartFor(attributesThatAreStartFor)
        .pagination(Pagination.builder().limit(limit).continueToken(continueToken).build())
        .build();
    var users = idmService.searchUsers(searchDto);
    usersResponseVariable.on(execution).set(users);
  }

  private IdmService getIdmService(String realm) {
    switch (realm) {
      case OFFICER_REALM:
        return officerIdmService;
      case CITIZEN_REALM:
        return citizenIdmService;
      default:
        throw new IllegalArgumentException("Realm must be one of ['officer', 'citizen']");
    }
  }

  @SuppressWarnings("unchecked")
  @Nullable
  private Map<String, List<String>> toMultivaluedMap(@Nullable Map<String, Object> map) {
    if (Objects.isNull(map)) {
      return null;
    }
    var multivaluedMap = new HashMap<String, List<String>>();
    map.forEach((key, value) -> {
      var multipleValue = new ArrayList<String>();
      if (value instanceof Collection) {
        ((Collection<Object>) value).stream().map(Object::toString).forEach(multipleValue::add);
      } else {
        multipleValue.add(value.toString());
      }
      multivaluedMap.put(key, multipleValue);
    });
    return multivaluedMap;
  }

  @Nullable
  private Integer toInteger(@Nullable String value) {
    return Objects.isNull(value) ? null : Integer.valueOf(value);
  }
}
