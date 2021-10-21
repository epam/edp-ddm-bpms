package com.epam.digital.data.platform.bpms.security;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.identity.Authentication;

/**
 * Class that is used for camunda user impersonation (acting on another user's behalf)
 */
@Slf4j
@Builder
@Getter
public class CamundaImpersonation {

  /**
   * Current camunda process engine
   */
  private final ProcessEngine processEngine;

  /**
   * Impersonatee (the user who is being impersonated by another) {@link Authentication} object
   */
  private final Authentication impersonatee;

  /**
   * Impersonator (a user who acts on another user's behalf) {@link Authentication} object
   */
  private final Authentication impersonator;

  /**
   * Authenticate using impersonatee user
   */
  public void impersonate() {
    processEngine.getIdentityService().setAuthentication(impersonatee);
  }

  /**
   * Authenticate back using impersonator user
   */
  public void revertToSelf() {
    processEngine.getIdentityService().setAuthentication(impersonator);
  }
}