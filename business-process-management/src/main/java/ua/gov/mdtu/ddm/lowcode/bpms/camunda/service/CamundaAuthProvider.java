package ua.gov.mdtu.ddm.lowcode.bpms.camunda.service;

import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CamundaAuthProvider {

  private static final String OFFICER = "officer";
  private static final Set<String> ALLOWED_AUTHORITIES = Set.of(OFFICER);

  private final IdentityService identityService;
  private final AuthorizationService authorizationService;

  public void clearAuthentication() {
    identityService.clearAuthentication();
    log.debug("Clear Camunda authentication");
  }

  public void createAuthentication(Authentication authentication) {
    if (Objects.isNull(authentication)) {
      log.debug("User is not authenticated in application");
      return;
    }
    if (hasAuthorities(ALLOWED_AUTHORITIES, authentication.getAuthorities())) {
      Group officerGroup = getOrCreateOfficerGroup();
      User camundaUser = getOrCreateCamundaUser(authentication.getName());
      identityService.setAuthentication(
          new org.camunda.bpm.engine.impl.identity.Authentication(camundaUser.getId(),
              Collections.singletonList(officerGroup.getId())));
    } else {
      //authenticate user but without any permissions
      identityService.setAuthenticatedUserId(authentication.getName());
    }
    log.debug("Camunda authentication is created for {}", authentication.getName());
  }

  private boolean hasAuthorities(Set<String> allowedAuthorities,
      Collection<? extends GrantedAuthority> authorities) {
    return authorities.stream()
        .anyMatch(grantedAuthority -> allowedAuthorities.contains(grantedAuthority.getAuthority()));
  }

  private Group getOrCreateOfficerGroup() {
    Group officerGroup = identityService.createGroupQuery().groupId(OFFICER).singleResult();
    if (Objects.isNull(officerGroup)) {
      return createGroupWithBaseAuthorizations(OFFICER);
    }
    return officerGroup;
  }

  private User getOrCreateCamundaUser(String userId) {
    User camundaUser = identityService.createUserQuery().userId(userId).singleResult();
    if (Objects.isNull(camundaUser)) {
      return createUserWithBaseAuthorizations(userId);
    }
    return camundaUser;
  }

  public void createAuthorization(String resourceId, Resources resources,
      Permission[] permissions, String userId, String groupId) {
    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setResource(resources);
    authorization.setResourceId(resourceId);
    authorization.setPermissions(permissions);
    authorization.setUserId(StringUtils.isEmpty(userId) ? null : userId);
    authorization.setGroupId(StringUtils.isEmpty(groupId) ? null : groupId);
    authorizationService.saveAuthorization(authorization);
  }

  private User createUserWithBaseAuthorizations(String userId) {
    User newUser = identityService.newUser(userId);
    identityService.saveUser(newUser);
    identityService.createMembership(userId, OFFICER);
    createAuthorization("*", Resources.PROCESS_INSTANCE, new Permission[]{Permissions.CREATE},
        userId, null);
    return newUser;
  }

  private Group createGroupWithBaseAuthorizations(String groupId) {
    Group createdGroup = identityService.newGroup(groupId);
    identityService.saveGroup(createdGroup);
    createAuthorization("*", Resources.PROCESS_DEFINITION,
        new Permission[]{ProcessDefinitionPermissions.CREATE_INSTANCE,
            ProcessDefinitionPermissions.READ}, null, groupId);
    return createdGroup;
  }
}
