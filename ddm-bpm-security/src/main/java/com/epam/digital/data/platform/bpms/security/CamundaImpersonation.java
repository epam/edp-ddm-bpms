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

import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.identity.Authentication;

/**
 * Class that is used for camunda user impersonation (acting on another user's behalf)
 */
@Slf4j
@Getter
@Builder
@AllArgsConstructor
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

  public CamundaImpersonation(ProcessEngine processEngine, Authentication impersonatee) {
    this.processEngine = processEngine;
    this.impersonatee = impersonatee;
    this.impersonator = processEngine.getIdentityService().getCurrentAuthentication();
  }

  /**
   * Authenticate using impersonatee user
   */
  public void impersonate() {
    log.trace("Authenticate as {} user", impersonatee.getGroupIds());
    processEngine.getIdentityService().setAuthentication(impersonatee);
  }

  /**
   * Authenticate back using impersonator user
   */
  public void revertToSelf() {
    log.trace("Authenticate back to current user");
    processEngine.getIdentityService().setAuthentication(impersonator);
  }

  /**
   * Executing retrieving data on impersonated behalf
   *
   * @param supplier supplier which needs to be executed on impersonated behalf
   * @param <T>      the supplier type
   * @return the supplier result
   */
  public <T> T execute(Supplier<T> supplier) {
    log.debug("Executing retrieving data on impersonated behalf");
    impersonate();
    try {
      var result = supplier.get();

      log.debug("Data retrieved on impersonated behalf");
      return result;
    } finally {
      revertToSelf();
    }
  }

  /**
   * Executing the process on impersonated behalf
   *
   * @param runnable the process itself
   */
  public void run(Runnable runnable) {
    log.debug("Executing process on impersonated behalf");
    impersonate();
    try {
      runnable.run();
      log.debug("The process finished on impersonated behalf");
    } finally {
      revertToSelf();
    }
  }
}