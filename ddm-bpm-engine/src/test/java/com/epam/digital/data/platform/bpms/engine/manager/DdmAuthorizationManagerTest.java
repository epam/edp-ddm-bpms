package com.epam.digital.data.platform.bpms.engine.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DdmAuthorizationManagerTest {

  @Spy
  private DdmAuthorizationManagerSpy ddmAuthorizationManager = new DdmAuthorizationManagerSpy(
      Set.of("officer", "citizen"));

  @Test
  void filterAuthenticatedGroupIds() {
    var result = ddmAuthorizationManager.filterAuthenticatedGroupIds(
        List.of("officer", "officer2"));

    assertThat(result).hasSize(1).contains("officer");

    verify(ddmAuthorizationManager, never()).getDbEntityManager();
  }

  private static class DdmAuthorizationManagerSpy extends DdmAuthorizationManager {

    public DdmAuthorizationManagerSpy(Set<String> availableAuthorizedGroupIds) {
      super(availableAuthorizedGroupIds);
    }

    @Override
    public DbEntityManager getDbEntityManager() {
      throw new IllegalStateException("getDbEntityManager method was called");
    }
  }

}
