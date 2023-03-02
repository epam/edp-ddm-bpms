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
public enum DdmBpmHistoryCleanupMetric implements DdmBpmMetric {
  REMOVED_PROCESS_INSTANCES_METRICS("camunda.history.cleanup.removed.process.instances",
      "The amount of deleted historical process-instances",
      e -> e.getManagementService().createMetricsQuery()
          .name(Metrics.HISTORY_CLEANUP_REMOVED_PROCESS_INSTANCES).sum()),
  REMOVED_TASKS_METRICS("camunda.history.cleanup.removed.task.metrics",
      "The amount of deleted historical tasks",
      e -> e.getManagementService().createMetricsQuery()
          .name(Metrics.HISTORY_CLEANUP_REMOVED_TASK_METRICS).sum());


  final String name;
  final String description;
  final ToDoubleFunction<ProcessEngine> metricFunction;
}
