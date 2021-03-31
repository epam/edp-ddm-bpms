package ua.gov.mdtu.ddm.lowcode.bpms.listener;

import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;

import java.util.Collections;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.ProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link ExecutionListener} listener that is used to set
 * authorizations for current user before process instance starts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationStartEventListener implements ExecutionListener {

  @Value("${camunda.admin-user-id}")
  private final String administratorUserId;
  @Value("${camunda.admin-group-id}")
  private final String administratorGroupName;

  @Override
  public void notify(DelegateExecution execution) throws Exception {
    log.info("AuthorizationStartEventListener started...");
    ProcessEngine processEngine = BpmPlatform.getDefaultProcessEngine();
    if (processEngine == null) {
      processEngine = ProcessEngines.getDefaultProcessEngine(false);
    }
    if (Objects.isNull(processEngine.getIdentityService().getCurrentAuthentication())) {
      log.warn("User is not authenticated in Camunda IdentityService");
      return;
    }
    Authentication currentAuthentication = processEngine.getIdentityService()
        .getCurrentAuthentication();

    //only admin user has rights for creation authorizations
    processEngine.getIdentityService().setAuthentication(
        new Authentication(administratorUserId, Collections.singletonList(administratorGroupName)));
    addPermissionForProcessInstances(processEngine, execution, currentAuthentication.getUserId());
    addPermissionReadHistory(processEngine, execution, currentAuthentication.getUserId());

    processEngine.getIdentityService().setAuthentication(currentAuthentication);
    log.info("AuthorizationStartEventListener finished...");
  }

  private void addPermissionForProcessInstances(ProcessEngine processEngine,
      DelegateExecution execution, String userId) {
    Authorization authorization = createAuthorization(processEngine,
        execution.getProcessInstanceId(),
        Resources.PROCESS_INSTANCE, new Permission[]{ProcessInstancePermissions.READ,
            ProcessInstancePermissions.UPDATE_VARIABLE}, userId);
    processEngine.getAuthorizationService().saveAuthorization(authorization);
  }

  private void addPermissionReadHistory(ProcessEngine processEngine, DelegateExecution execution,
      String userId) {
    Authorization authorization = createAuthorization(processEngine,
        execution.getProcessInstanceId(),
        Resources.PROCESS_DEFINITION,
        new Permission[]{ProcessDefinitionPermissions.READ_HISTORY,
            ProcessDefinitionPermissions.READ_HISTORY_VARIABLE}, userId);
    processEngine.getAuthorizationService().saveAuthorization(authorization);
  }

  private Authorization createAuthorization(ProcessEngine processEngine, String resourceId,
      Resources resources, Permission[] permissions, String userId) {
    Authorization authorization = processEngine.getAuthorizationService()
        .createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setResource(resources);
    authorization.setResourceId(resourceId);
    authorization.setPermissions(permissions);
    authorization.setUserId(userId);
    return authorization;
  }
}
