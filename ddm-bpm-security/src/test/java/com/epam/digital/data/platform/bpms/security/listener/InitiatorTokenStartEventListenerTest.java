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

package com.epam.digital.data.platform.bpms.security.listener;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.initiator.InitiatorVariablesAccessor;
import com.epam.digital.data.platform.dataaccessor.initiator.InitiatorVariablesWriteAccessor;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class InitiatorTokenStartEventListenerTest {

  @InjectMocks
  private InitiatorTokenStartEventListener initiatorTokenStartEventListener;
  @Mock
  private ExecutionEntity execution;
  @Mock
  private InitiatorVariablesAccessor initiatorVariablesAccessor;
  @Mock
  private InitiatorVariablesWriteAccessor initiatorVariablesWriteAccessor;

  @BeforeEach
  public void setUp() {
    when(initiatorVariablesAccessor.on(execution)).thenReturn(initiatorVariablesWriteAccessor);
  }

  @Test
  void testNotAuthenticated() {
    SecurityContextHolder.getContext().setAuthentication(null);

    initiatorTokenStartEventListener.notify(execution);

    verify(initiatorVariablesWriteAccessor).setInitiatorAccessToken(null);
  }

  @Test
  void testAuthenticatedNotByStringToken() {
    var notStringCredentials = new Object();
    var auth = new UsernamePasswordAuthenticationToken("user", notStringCredentials);
    SecurityContextHolder.getContext().setAuthentication(auth);

    initiatorTokenStartEventListener.notify(execution);

    verify(initiatorVariablesWriteAccessor).setInitiatorAccessToken(null);
  }

  @Test
  void testAuthenticated() {
    var stringCredentials = "token";
    var auth = new UsernamePasswordAuthenticationToken("user", stringCredentials);
    SecurityContextHolder.getContext().setAuthentication(auth);

    initiatorTokenStartEventListener.notify(execution);

    verify(initiatorVariablesWriteAccessor).setInitiatorAccessToken(stringCredentials);
  }
}
