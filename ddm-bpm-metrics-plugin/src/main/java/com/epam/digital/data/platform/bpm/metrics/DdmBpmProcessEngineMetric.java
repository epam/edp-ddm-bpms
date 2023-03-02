/*
 * Copyright 2023 EPAM Systems.
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

package com.epam.digital.data.platform.bpm.metrics;

import java.util.function.ToDoubleFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;

@Getter
@RequiredArgsConstructor
public enum DdmBpmProcessEngineMetric implements DdmBpmMetric {
  USER_COUNT_METRIC("camunda.user.count",
      "The total amount of camunda users",
      e -> e.getIdentityService().createUserQuery().count()),
  AUTHORIZATION_COUNT_METRIC("camunda.authorization.count",
      "The total amount of camunda authorizations",
      e -> e.getAuthorizationService().createAuthorizationQuery().count()),
  DEPLOYMENTS_METRIC("camunda.deployments",
      "The total amount of camunda deployments",
      e -> e.getRepositoryService().createDeploymentQuery().count()),
  ACTIVE_PROCESS_DEFINITIONS_METRIC("camunda.active.process.definitions",
      "The total amount of active camunda process-definitions",
      e -> e.getRepositoryService().createProcessDefinitionQuery().active().count());

  private final String name;
  private final String description;
  private final ToDoubleFunction<ProcessEngine> metricFunction;
}
