package com.epam.digital.data.platform.bpms.security;

import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;

import com.epam.digital.data.platform.starter.security.SystemRole;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.Resources;
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
  private final AuthorizationService authorizationService;

  @PostConstruct
  public void setUp() {
    Stream.of(SystemRole.getRoleNames())
        .forEach(this::createGroupWithBaseAuthorizationsIfNotExists);
  }

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

  private void createGroupWithBaseAuthorizationsIfNotExists(String groupId) {
    if (Objects.nonNull(identityService.createGroupQuery().groupId(groupId).singleResult())) {
      return;
    }

    var createdGroup = identityService.newGroup(groupId);
    identityService.saveGroup(createdGroup);
    createAuthorization(Resources.PROCESS_DEFINITION,
        new Permission[]{ProcessDefinitionPermissions.CREATE_INSTANCE,
            ProcessDefinitionPermissions.READ}, groupId);
    createAuthorization(Resources.PROCESS_INSTANCE, new Permission[]{Permissions.CREATE}, groupId);
  }

  private void createAuthorization(Resources resources, Permission[] permissions, String groupId) {
    var authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setResource(resources);
    authorization.setResourceId("*");
    authorization.setPermissions(permissions);
    authorization.setUserId(null);
    authorization.setGroupId(groupId);
    authorizationService.saveAuthorization(authorization);
  }
}
