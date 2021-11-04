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
    var camundaImpersonation = CamundaImpersonation.builder()
        .processEngine(processEngine)
        .impersonator(impersonator)
        .impersonatee(impersonatee)
        .build();

    camundaImpersonation.impersonate();
    camundaImpersonation.revertToSelf();

    verify(identityService, times(1)).setAuthentication(impersonatee);
    verify(identityService, times(1)).setAuthentication(impersonator);
  }
}