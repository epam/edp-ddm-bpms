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
import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Base class for all camunda metrics that are registered by
 * {@link com.epam.digital.data.platform.bpm.metrics.binder.CamundaMeterBinder}
 */
public interface DdmBpmMetric {

  /**
   * @return metric name that will be used by Prometheus
   */
  @NonNull
  String getName();

  /**
   * @return metrics description
   */
  @Nullable
  String getDescription();

  /**
   * @return function over camunda {@link ProcessEngine} that has to be used for reading the metric
   * value
   */
  ToDoubleFunction<ProcessEngine> getMetricFunction();
}
