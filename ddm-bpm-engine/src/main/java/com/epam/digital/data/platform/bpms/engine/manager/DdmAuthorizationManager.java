package com.epam.digital.data.platform.bpms.engine.manager;

import java.util.Set;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;

/**
 * Child of {@link AuthorizationManager} that is used for overriding authorization sql queries.
 * <p>
 * Defines {@link AuthorizationManager#availableAuthorizedGroupIds} in constructor to reduce
 * selecting groups from authorization database relation
 */
public class DdmAuthorizationManager extends AuthorizationManager {

  public DdmAuthorizationManager(Set<String> availableAuthorizedGroupIds) {
    this.availableAuthorizedGroupIds = availableAuthorizedGroupIds;
  }
}
