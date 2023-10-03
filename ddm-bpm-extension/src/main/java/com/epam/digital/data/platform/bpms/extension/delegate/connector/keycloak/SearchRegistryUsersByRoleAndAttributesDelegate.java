/*
 * Copyright 2025 EPAM Systems.
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
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByRoleAndAttributesRequestDto;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByRoleAndAttributesRequestDto.OffsetPagination;
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
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component(SearchRegistryUsersByRoleAndAttributesDelegate.DELEGATE_NAME)
public class SearchRegistryUsersByRoleAndAttributesDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "searchRegistryUsersByRoleAndAttributes";

  private static final String OFFICER_REALM = "officer";
  private static final String CITIZEN_REALM = "citizen";
  private static final Integer DEFAULT_OFFSET = 0;
  private static final Integer MAX_LIMIT = 100;

  @SystemVariable(name = "realm")
  private NamedVariableAccessor<String> realmVariable;
  @SystemVariable(name = "status")
  private NamedVariableAccessor<String> statusVariable;
  @SystemVariable(name = "username")
  private NamedVariableAccessor<String> usernameVariable;
  @SystemVariable(name = "role_name")
  private NamedVariableAccessor<String> roleNameVariable;
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
  private NamedVariableAccessor<Object> limitVariable;
  @SystemVariable(name = "offset")
  private NamedVariableAccessor<Object> offsetVariable;

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
    var status = statusVariable.from(execution).getLocal();
    var username = usernameVariable.from(execution).getLocal();
    var roleName = roleNameVariable.from(execution).getLocal();
    var attributesEquals = toMultivaluedMap(attributesEqualsVariable.from(execution).getLocal());
    var attributesStartWith = toMultivaluedMap(
        attributesStartWithVariable.from(execution).getLocal());
    var attributesThatAreStartFor = toMultivaluedMap(
        attributesThatAreStartForVariable.from(execution).getLocal());
    var limit = toInteger(limitVariable.from(execution).getLocal());
    var offset = toInteger(offsetVariable.from(execution).getLocal());

    var searchDto = SearchUsersByRoleAndAttributesRequestDto.builder()
        .enabled(toBoolean(status))
        .username(username)
        .roleName(roleName)
        .attributesEquals(attributesEquals)
        .attributesStartsWith(attributesStartWith)
        .attributesThatAreStartFor(attributesThatAreStartFor)
        .pagination(OffsetPagination.builder()
            .limit(normalizeLimit(limit))
            .offset(normalizeOffset(offset))
            .build())
        .build();
    var users = idmService.searchUsersByRoleAndAttributes(searchDto);
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
  private Integer toInteger(@Nullable Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Integer) {
      return (Integer) value;
    }
    if (value instanceof String) {
      return Integer.parseInt((String) value);
    }
    throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName() + ", value: " + value);
  }

  @Nullable
  private Boolean toBoolean(@Nullable String value) {
    if (Objects.isNull(value) || "all".equalsIgnoreCase(value)) {
      return null;
    }
    return Boolean.parseBoolean(value);
  }

  private int normalizeLimit(Integer limit) {
    return (Objects.nonNull(limit) && limit >= 0 && limit <= MAX_LIMIT) ? limit : MAX_LIMIT;
  }

  private int normalizeOffset(Integer offset) {
    return (Objects.nonNull(offset) && offset >= DEFAULT_OFFSET) ? offset : DEFAULT_OFFSET;
  }

}
