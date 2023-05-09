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

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableReadAccessor;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableWriteAccessor;
import com.epam.digital.data.platform.integration.idm.model.IdmUsersResponse;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByAttributesRequestDto;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SearchRegistryUsersByAttributesDelegateTest {

  @InjectMocks
  private SearchRegistryUsersByAttributesDelegate delegate;
  @Mock
  private IdmService officerIdmService;
  @Mock
  private IdmService citizenIdmService;

  @Mock
  private NamedVariableReadAccessor<String> realmVariableReadAccessor;
  @Mock
  private NamedVariableReadAccessor<Map<String, Object>> attributesEqualsReadAccessor;
  @Mock
  private NamedVariableReadAccessor<Map<String, Object>> attributesStartWithReadAccessor;
  @Mock
  private NamedVariableReadAccessor<Map<String, Object>> attributesThatAreStartForReadAccessor;
  @Mock
  private NamedVariableReadAccessor<String> limitReadAccessor;
  @Mock
  private NamedVariableReadAccessor<String> continueTokenReadAccessor;
  @Mock
  private NamedVariableWriteAccessor<IdmUsersResponse> usersResponseWriteAccessor;

  @Mock
  private DelegateExecution delegateExecution;

  @Captor
  private ArgumentCaptor<SearchUsersByAttributesRequestDto> captor;

  @BeforeEach
  void setUp() {
    delegate = new SearchRegistryUsersByAttributesDelegate(officerIdmService, citizenIdmService);

    var realmVariable = Mockito.mock(NamedVariableAccessor.class);
    Mockito.lenient().doReturn(realmVariableReadAccessor).when(realmVariable)
        .from(delegateExecution);
    var attributesEqualsVariable = Mockito.mock(NamedVariableAccessor.class);
    Mockito.lenient().doReturn(attributesEqualsReadAccessor).when(attributesEqualsVariable)
        .from(delegateExecution);
    var attributesStartWithVariable = Mockito.mock(NamedVariableAccessor.class);
    Mockito.lenient().doReturn(attributesStartWithReadAccessor).when(attributesStartWithVariable)
        .from(delegateExecution);
    var attributesThatAreStartForVariable = Mockito.mock(NamedVariableAccessor.class);
    Mockito.lenient().doReturn(attributesThatAreStartForReadAccessor)
        .when(attributesThatAreStartForVariable).from(delegateExecution);
    var limitVariable = Mockito.mock(NamedVariableAccessor.class);
    Mockito.lenient().doReturn(limitReadAccessor).when(limitVariable).from(delegateExecution);
    var continueTokenVariable = Mockito.mock(NamedVariableAccessor.class);
    Mockito.lenient().doReturn(continueTokenReadAccessor).when(continueTokenVariable)
        .from(delegateExecution);
    var usersResponseVariable = Mockito.mock(NamedVariableAccessor.class);
    Mockito.lenient().doReturn(usersResponseWriteAccessor).when(usersResponseVariable)
        .on(delegateExecution);

    ReflectionTestUtils.setField(delegate, "realmVariable", realmVariable);
    ReflectionTestUtils.setField(delegate, "attributesEqualsVariable", attributesEqualsVariable);
    ReflectionTestUtils.setField(delegate, "attributesStartWithVariable",
        attributesStartWithVariable);
    ReflectionTestUtils.setField(delegate, "attributesThatAreStartForVariable",
        attributesThatAreStartForVariable);
    ReflectionTestUtils.setField(delegate, "limitVariable", limitVariable);
    ReflectionTestUtils.setField(delegate, "continueTokenVariable", continueTokenVariable);
    ReflectionTestUtils.setField(delegate, "usersResponseVariable", usersResponseVariable);
  }

  @Test
  void testGetName() {
    assertThat(delegate.getDelegateName()).isEqualTo(
        SearchRegistryUsersByAttributesDelegate.DELEGATE_NAME);
  }

  @Test
  @SneakyThrows
  void testOfficerRequest() {
    Mockito.doReturn("officer").when(realmVariableReadAccessor).getOrThrow();
    Mockito.doReturn(Map.of("attr", "value")).when(attributesEqualsReadAccessor).get();
    Mockito.doReturn(Map.of("attr2", Set.of(2))).when(attributesStartWithReadAccessor).get();
    Mockito.doReturn("2").when(limitReadAccessor).get();

    delegate.execute(delegateExecution);

    Mockito.verify(officerIdmService).searchUsers(captor.capture());
    Mockito.verifyNoMoreInteractions(officerIdmService, citizenIdmService);

    var actualRequest = captor.getValue();
    Assertions.assertThat(actualRequest)
        .hasFieldOrPropertyWithValue("attributesEquals", Map.of("attr", List.of("value")))
        .hasFieldOrPropertyWithValue("attributesStartsWith", Map.of("attr2", List.of("2")))
        .hasFieldOrPropertyWithValue("attributesThatAreStartFor", null)
        .extracting(SearchUsersByAttributesRequestDto::getPagination)
        .hasFieldOrPropertyWithValue("limit", 2).hasFieldOrPropertyWithValue("continueToken", null);
  }

  @Test
  @SneakyThrows
  void testCitizenRequest() {
    Mockito.doReturn("citizen").when(realmVariableReadAccessor).getOrThrow();
    Mockito.doReturn(Map.of()).when(attributesEqualsReadAccessor).get();
    Mockito.doReturn(Map.of("attr", List.of())).when(attributesStartWithReadAccessor).get();
    Mockito.doReturn(Map.of("attr2", List.of("value2"))).when(attributesThatAreStartForReadAccessor)
        .get();
    Mockito.doReturn("2").when(limitReadAccessor).get();
    Mockito.doReturn("12").when(continueTokenReadAccessor).get();

    delegate.execute(delegateExecution);

    Mockito.verify(citizenIdmService).searchUsers(captor.capture());
    Mockito.verifyNoMoreInteractions(officerIdmService, citizenIdmService);

    var actualRequest = captor.getValue();
    Assertions.assertThat(actualRequest).hasFieldOrPropertyWithValue("attributesEquals", Map.of())
        .hasFieldOrPropertyWithValue("attributesStartsWith", Map.of("attr", List.of()))
        .hasFieldOrPropertyWithValue("attributesThatAreStartFor",
            Map.of("attr2", List.of("value2")))
        .extracting(SearchUsersByAttributesRequestDto::getPagination)
        .hasFieldOrPropertyWithValue("limit", 2).hasFieldOrPropertyWithValue("continueToken", 12);
  }

  @Test
  @SneakyThrows
  void testIllegalArgument() {
    Mockito.doReturn("illegal").when(realmVariableReadAccessor).getOrThrow();

    Assertions.assertThatThrownBy(() -> delegate.execute(delegateExecution))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Realm must be one of ['officer', 'citizen']");

    Mockito.verifyNoMoreInteractions(officerIdmService, citizenIdmService);
  }
}
