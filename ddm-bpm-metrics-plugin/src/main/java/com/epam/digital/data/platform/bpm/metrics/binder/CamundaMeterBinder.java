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

package com.epam.digital.data.platform.bpm.metrics.binder;

import com.epam.digital.data.platform.bpm.metrics.DdmBpmAsyncJobsMetric;
import com.epam.digital.data.platform.bpm.metrics.DdmBpmBusinessProcessMetric;
import com.epam.digital.data.platform.bpm.metrics.DdmBpmHistoryCleanupMetric;
import com.epam.digital.data.platform.bpm.metrics.DdmBpmMetric;
import com.epam.digital.data.platform.bpm.metrics.DdmBpmProcessEngineMetric;
import com.epam.digital.data.platform.bpm.metrics.DdmBpmSubscriptionsMetric;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.lang.NonNull;

/**
 * Implementation of {@link MeterBinder} that registers Camunda metrics.
 *
 * @see DdmBpmProcessEngineMetric Process engine metrics
 * @see DdmBpmBusinessProcessMetric Business-process metrics
 * @see DdmBpmSubscriptionsMetric Subscriptions metrics
 * @see DdmBpmAsyncJobsMetric Async jobs metrics
 * @see DdmBpmHistoryCleanupMetric History cleanup metrics
 */
@RequiredArgsConstructor
public class CamundaMeterBinder implements MeterBinder {

  private final ProcessEngine processEngine;

  @Override
  public void bindTo(@NonNull MeterRegistry registry) {
    Stream.of(
            DdmBpmProcessEngineMetric.values(),
            DdmBpmBusinessProcessMetric.values(),
            DdmBpmSubscriptionsMetric.values(),
            DdmBpmAsyncJobsMetric.values(),
            DdmBpmHistoryCleanupMetric.values()
        )
        .flatMap(Stream::of)
        .forEach(metric -> registerMetric(registry, metric));
  }

  private void registerMetric(MeterRegistry registry, DdmBpmMetric metric) {
    Gauge.builder(metric.getName(),
            () -> metric.getMetricFunction().applyAsDouble(processEngine))
        .description(metric.getDescription())
        .register(registry);
  }
}
