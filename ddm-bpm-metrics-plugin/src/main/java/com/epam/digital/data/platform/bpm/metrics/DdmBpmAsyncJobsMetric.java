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
public enum DdmBpmAsyncJobsMetric implements DdmBpmMetric {
  MESSAGE_JOBS_METRIC("camunda.message.jobs",
      "The amount of jobs that are messages",
      e -> e.getManagementService().createJobQuery().messages().count()),
  TIMER_JOBS_METRIC("camunda.timer.jobs",
      "The amount of jobs that are timers",
      e -> e.getManagementService().createJobQuery().timers().count()),
  EXECUTABLE_TIMER_JOBS_METRIC("camunda.executable.timer.jobs",
      "The amount of jobs that are executable timers",
      e -> e.getManagementService().createJobQuery().timers().executable().count()),
  EXECUTABLE_JOBS_METRIC("camunda.executable.jobs",
      "The amount of jobs that are executable",
      e -> e.getManagementService().createJobQuery().executable().count());


  final String name;
  final String description;
  final ToDoubleFunction<ProcessEngine> metricFunction;
}
