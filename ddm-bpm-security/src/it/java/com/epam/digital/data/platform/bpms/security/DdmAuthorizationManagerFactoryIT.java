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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.security.manager.DdmAuthorizationManager;
import com.epam.digital.data.platform.bpms.security.manager.factory.DdmAuthorizationManagerFactory;
import java.util.List;
import javax.inject.Inject;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.junit.jupiter.api.Test;

class DdmAuthorizationManagerFactoryIT extends BaseIT {

  @Inject
  private DdmAuthorizationManagerFactory ddmAuthorizationManagerFactory;

  @Test
  void filterAuthenticatedGroupIds() {
    var userRoles = List.of("officer", "head-officer", "developer");
    var authorizationManager = ddmAuthorizationManagerFactory.openSession();

    var resultRoles = authorizationManager.filterAuthenticatedGroupIds(userRoles);

    assertThat(resultRoles).hasSize(2)
        .contains("officer", "head-officer");

    var commandContext = mock(CommandContext.class);
    Context.setCommandContext(commandContext);
    var ex = new IllegalStateException("Db was called");
    when(commandContext.getSession(DbEntityManager.class)).thenThrow(ex);
    var ddmAuthorizationManager = new DdmAuthorizationManager(null);
    var result = assertThrows(IllegalStateException.class,
        () -> ddmAuthorizationManager.filterAuthenticatedGroupIds(userRoles));

    assertThat(result).isSameAs(ex);
  }
}
