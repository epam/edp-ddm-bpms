package com.epam.digital.data.platform.bpms.listener;

import static org.mockito.Mockito.verify;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class InitiatorTokenStartEventListenerTest {

  @InjectMocks
  private InitiatorTokenStartEventListener initiatorTokenStartEventListener;
  @Mock
  private ExecutionEntity execution;

  @Test
  public void testNotAuthenticated() {
    SecurityContextHolder.getContext().setAuthentication(null);

    initiatorTokenStartEventListener.notify(execution);

    verify(execution).setVariableLocalTransient("initiator_access_token", null);
  }

  @Test
  public void testAuthenticatedNotByStringToken() {
    var notStringCredentials = new Object();
    var auth = new UsernamePasswordAuthenticationToken("user", notStringCredentials);
    SecurityContextHolder.getContext().setAuthentication(auth);

    initiatorTokenStartEventListener.notify(execution);

    verify(execution).setVariableLocalTransient("initiator_access_token", null);
  }

  @Test
  public void testAuthenticated() {
    var stringCredentials = "token";
    var auth = new UsernamePasswordAuthenticationToken("user", stringCredentials);
    SecurityContextHolder.getContext().setAuthentication(auth);

    initiatorTokenStartEventListener.notify(execution);

    verify(execution).setVariableLocalTransient("initiator_access_token", stringCredentials);
  }
}
