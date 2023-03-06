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
  COMPLETED_ROOT_PROCESS_INSTANCES_METRIC("camunda.root.process.instances.completed",
      "The amount of completed process-instances",
      e -> e.getHistoryService().createHistoricProcessInstanceQuery().rootProcessInstances()
          .completed().count()),
  TERMINATED_ROOT_PROCESS_INSTANCES_METRIC("camunda.root.process.instances.terminated",
      "The amount of externally-terminated process-instances",
      e -> e.getHistoryService().createHistoricProcessInstanceQuery().rootProcessInstances()
          .externallyTerminated().count()),
  SUSPENDED_ROOT_PROCESS_INSTANCES_METRIC("camunda.root.process.instances.suspended",
      "The amount of suspended process-instances",
      e -> e.getHistoryService().createHistoricProcessInstanceQuery().rootProcessInstances()
          .suspended().count()),
  ACTIVE_ROOT_PROCESS_INSTANCES_METRIC("camunda.root.process.instances.active",
      "The amount of active process-instances",
      e -> e.getHistoryService().createHistoricProcessInstanceQuery().rootProcessInstances()
          .active().count()),
  ROOT_PROCESS_INSTANCES_METRIC("camunda.root.process.instances",
      "The total amount of all process-instances",
      e -> e.getHistoryService().createHistoricProcessInstanceQuery().rootProcessInstances()
          .count()),
  ACTIVE_INCIDENTS_METRIC("camunda.active.incidents",
      "The amount of active process-instance incidents",
      e -> e.getRuntimeService().createIncidentQuery().count());


  final String name;
  final String description;
  final ToDoubleFunction<ProcessEngine> metricFunction;
}
