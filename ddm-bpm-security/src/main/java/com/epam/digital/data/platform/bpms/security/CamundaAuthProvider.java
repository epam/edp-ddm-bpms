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

import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * The class represents a provider that is used to manage camunda authentication for user.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CamundaAuthProvider {

  private final IdentityService identityService;

  /**
   * Method allows clearing the current camunda authentication for user
   */
  public void clearAuthentication() {
    identityService.clearAuthentication();
    log.debug("Clear Camunda authentication");
  }

  /**
   * Method for creating camunda authentication for user
   *
   * @param authentication {@link Authentication} object
   */
  public void createAuthentication(Authentication authentication) {
    if (Objects.isNull(authentication)) {
      log.debug("User is not authenticated in application");
      return;
    }

    var roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());
    identityService.setAuthentication(authentication.getName(), roles);

    log.debug("Camunda authentication is created for {}", authentication.getName());
  }
}
