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
import org.camunda.bpm.engine.management.Metrics;

@Getter
@RequiredArgsConstructor
public enum DdmBpmBusinessProcessMetric implements DdmBpmMetric {
  ACTIVE_USER_TASKS_METRIC("camunda.active.user.tasks",
      "The total amount of active camunda user tasks",
      e -> e.getTaskService().createTaskQuery().active().count()),
  ACTIVE_USER_TASKS_ASSIGNED_METRIC("camunda.active.user.tasks.assigned",
      "The amount of active camunda user tasks that have an assignee",
      e -> e.getTaskService().createTaskQuery().active().taskAssigned().count()),
  ACTIVE_USER_TASKS_UNASSIGNED_METRIC("camunda.active.user.tasks.unassigned",
      "The amount of active camunda user tasks that don't have an assignee",
      e -> e.getTaskService().createTaskQuery().active().taskUnassigned().count()),
  COMPLETED_PROCESS_INSTANCES_METRIC("camunda.completed.process.instances",
      "The amount of completed process-instances",
      e -> e.getHistoryService().createHistoricProcessInstanceQuery().completed().count()),
  TERMINATED_PROCESS_INSTANCES_METRIC("camunda.terminated.process.instances",
      "The amount of externally-terminated process-instances",
      e -> e.getHistoryService().createHistoricProcessInstanceQuery().externallyTerminated()
          .count()),
  SUSPENDED_PROCESS_INSTANCES_METRIC("camunda.suspended.process.instances",
      "The amount of suspended process-instances",
      e -> e.getRuntimeService().createProcessInstanceQuery().suspended().count()),
  ACTIVE_PROCESS_INSTANCES_METRIC("camunda.active.process.instances",
      "The amount of active process-instances",
      e -> e.getRuntimeService().createProcessInstanceQuery().active().count()),
  PROCESS_INSTANCES_METRIC("camunda.process.instances.total",
      "The total amount of all process-instances",
      e -> e.getManagementService().createMetricsQuery().name(Metrics.ROOT_PROCESS_INSTANCE_START)
          .sum()),
  ACTIVE_INCIDENTS_METRIC("camunda.active.incidents",
      "The amount of active process-instance incidents",
      e -> e.getRuntimeService().createIncidentQuery().count());


  final String name;
  final String description;
  final ToDoubleFunction<ProcessEngine> metricFunction;
}
