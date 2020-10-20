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

package com.epam.digital.data.platform.bpms.security.manager;

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
