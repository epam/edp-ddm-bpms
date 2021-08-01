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
