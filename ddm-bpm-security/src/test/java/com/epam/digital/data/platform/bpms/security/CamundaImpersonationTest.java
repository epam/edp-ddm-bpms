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

package com.epam.digital.data.platform.bpms.security;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CamundaImpersonationTest {

  @Mock
  private ProcessEngine processEngine;
  @Mock
  private IdentityService identityService;

  @Test
  public void testImpersonation() {
    var impersonator = new Authentication("impersonator", null);
    var impersonatee = new Authentication("impersonatee", null);
    when(processEngine.getIdentityService()).thenReturn(identityService);
    when(identityService.getCurrentAuthentication()).thenReturn(impersonator);
    var camundaImpersonation = new CamundaImpersonation(processEngine, impersonatee);

    camundaImpersonation.impersonate();
    camundaImpersonation.revertToSelf();

    verify(identityService, times(1)).setAuthentication(impersonatee);
    verify(identityService, times(1)).setAuthentication(impersonator);
  }
}