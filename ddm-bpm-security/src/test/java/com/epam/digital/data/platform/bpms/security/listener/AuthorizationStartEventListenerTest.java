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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.security.CamundaImpersonation;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonationFactory;
import java.util.Optional;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationStartEventListenerTest {

  @InjectMocks
  private AuthorizationStartEventListener authorizationStartEventListener;
  @Mock
  private ExecutionEntity execution;
  @Mock
  private CamundaImpersonationFactory camundaImpersonationFactory;
  @Mock
  private ProcessEngine processEngine;
  @Mock
  private AuthorizationService authorizationService;
  @Mock
  private CamundaImpersonation camundaImpersonation;
  @Mock
  private Authentication impersonator;

  @Test
  public void testAuthorizationCreation() throws Exception {
    var userId = "userId";
    when(camundaImpersonationFactory.getCamundaImpersonation()).thenReturn(
        Optional.of(camundaImpersonation));
    when(camundaImpersonation.getImpersonator()).thenReturn(impersonator);
    when(impersonator.getUserId()).thenReturn(userId);
    when(camundaImpersonation.getProcessEngine()).thenReturn(processEngine);
    when(processEngine.getAuthorizationService()).thenReturn(authorizationService);
    when(authorizationService.createNewAuthorization(AUTH_TYPE_GRANT)).thenReturn(
        new AuthorizationEntity(1));

    authorizationStartEventListener.notify(execution);

    var captor = ArgumentCaptor.forClass(AuthorizationEntity.class);
    verify(authorizationService, times(1)).saveAuthorization(captor.capture());
    captor.getAllValues().forEach(a -> {
      assertThat(a.getUserId()).isEqualTo(userId);
      assertThat(a.getResource()).isEqualTo(Resources.PROCESS_INSTANCE.resourceType());
    });
  }
}