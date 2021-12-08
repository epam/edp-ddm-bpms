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

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.identity.Authentication;

/**
 * Class that is used for creating {@link CamundaImpersonation} object for current user that will be
 * acting on {@link CamundaImpersonationFactory#userId} behalf
 */
@Slf4j
@RequiredArgsConstructor
public class CamundaImpersonationFactory {

  /**
   * Camunda impersonatee user id
   */
  private final String userId;

  /**
   * Camunda impersonatee group name
   */
  private final String groupId;

  /**
   * Create impersonation for current authenticated user
   *
   * @return {@link CamundaImpersonation} object (optional, that is empty if there is no current
   * authenticated user)
   */
  public Optional<CamundaImpersonation> getCamundaImpersonation() {
    log.debug("Impersonation creating started...");
    var processEngine = BpmPlatform.getDefaultProcessEngine();
    if (Objects.isNull(processEngine)) {
      processEngine = ProcessEngines.getDefaultProcessEngine(false);
    }

    if (Objects.isNull(processEngine.getIdentityService().getCurrentAuthentication())) {
      log.warn("User is not authenticated in Camunda IdentityService");
      return Optional.empty();
    }
    var impersonator = processEngine.getIdentityService().getCurrentAuthentication();
    var impersonatee = new Authentication(userId, Collections.singletonList(groupId));
    return Optional.of(CamundaImpersonation.builder()
        .processEngine(processEngine)
        .impersonatee(impersonatee)
        .impersonator(impersonator)
        .build());
  }
}
