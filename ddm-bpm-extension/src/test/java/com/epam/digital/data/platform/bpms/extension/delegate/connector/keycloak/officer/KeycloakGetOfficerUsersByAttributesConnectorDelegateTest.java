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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableReadAccessor;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableWriteAccessor;
import com.epam.digital.data.platform.integration.idm.model.IdmUser;
import com.epam.digital.data.platform.integration.idm.model.SearchUserQuery;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import java.util.List;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class KeycloakGetOfficerUsersByAttributesConnectorDelegateTest {

  @InjectMocks
  private KeycloakGetOfficerUsersByAttributesConnectorDelegate delegate;
  @Mock
  private IdmService idmService;

  @Mock
  private NamedVariableAccessor<String> edrpouVariable;
  @Mock
  private NamedVariableReadAccessor<String> edrpouVariableReadAccessor;
  @Mock
  private NamedVariableAccessor<String> drfoVariable;
  @Mock
  private NamedVariableReadAccessor<String> drfoVariableReadAccessor;
  @Mock
  private NamedVariableAccessor<List<String>> usersByAttributeVariable;
  @Mock
  private NamedVariableWriteAccessor<List<String>> usersByAttributeVariableWriteAccessor;

  @Mock
  private DelegateExecution delegateExecution;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(delegate, "edrpouVariable", edrpouVariable);
    ReflectionTestUtils.setField(delegate, "drfoVariable", drfoVariable);
    ReflectionTestUtils.setField(delegate, "usersByAttributeVariable", usersByAttributeVariable);

    lenient().when(edrpouVariable.from(delegateExecution)).thenReturn(edrpouVariableReadAccessor);
    lenient().when(drfoVariable.from(delegateExecution)).thenReturn(drfoVariableReadAccessor);
    lenient().when(usersByAttributeVariable.on(delegateExecution))
        .thenReturn(usersByAttributeVariableWriteAccessor);
  }

  @Test
  void testGetName() {
    assertThat(delegate.getDelegateName()).isEqualTo(
        KeycloakGetOfficerUsersByAttributesConnectorDelegate.DELEGATE_NAME);
  }

  @Test
  void testNoEdrpou() {
    when(edrpouVariableReadAccessor.getOptional()).thenReturn(Optional.empty());
    when(delegateExecution.getProcessDefinitionId()).thenReturn("processDefinitionId");

    var nullEx = assertThrows(IllegalArgumentException.class,
        () -> delegate.execute(delegateExecution));

    assertThat(nullEx.getMessage()).isEqualTo(
        "Edrpou wasn't specified for keycloakGetOfficerUsersByAttributesConnectorDelegate "
            + "delegate in process with id processDefinitionId");

    when(edrpouVariableReadAccessor.getOptional()).thenReturn(Optional.of(""));
    when(delegateExecution.getProcessDefinitionId()).thenReturn("processDefinitionId");

    var emptyEx = assertThrows(IllegalArgumentException.class,
        () -> delegate.execute(delegateExecution));

    assertThat(emptyEx.getMessage()).isEqualTo(
        "Edrpou wasn't specified for keycloakGetOfficerUsersByAttributesConnectorDelegate "
            + "delegate in process with id processDefinitionId");
  }

  @Test
  void testHappyPathWithEmptyDrfo() throws Exception {
    when(edrpouVariableReadAccessor.getOptional()).thenReturn(Optional.of("edrpou"));
    when(drfoVariableReadAccessor.getOptional()).thenReturn(Optional.empty());

    var nullExpectedUsers = List.of(IdmUser.builder().userName("username1").build(),
        IdmUser.builder().userName("username2").build());
    when(idmService.searchUsers(SearchUserQuery.builder().edrpou("edrpou").build()))
        .thenReturn(nullExpectedUsers);

    delegate.execute(delegateExecution);

    when(drfoVariableReadAccessor.getOptional()).thenReturn(Optional.empty());

    delegate.execute(delegateExecution);

    verify(usersByAttributeVariableWriteAccessor, times(2)).set(List.of("username1", "username2"));
  }

  @Test
  void testHappyPath() throws Exception {
    when(edrpouVariableReadAccessor.getOptional()).thenReturn(Optional.of("edrpou"));
    when(drfoVariableReadAccessor.getOptional()).thenReturn(Optional.of("drfo"));

    var expectedUsers = List.of(IdmUser.builder().userName("username1").build(),
        IdmUser.builder().userName("username2").build());
    when(idmService.searchUsers(SearchUserQuery.builder().edrpou("edrpou").drfo("drfo").build()))
        .thenReturn(expectedUsers);

    delegate.execute(delegateExecution);

    verify(usersByAttributeVariableWriteAccessor).set(List.of("username1", "username2"));
  }
}
