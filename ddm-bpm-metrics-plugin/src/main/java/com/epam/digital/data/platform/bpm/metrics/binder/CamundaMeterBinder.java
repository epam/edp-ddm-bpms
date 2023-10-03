/*
 * Copyright 2025 EPAM Systems.
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Meter binder for Camunda BPM metrics. This class handles collecting metrics from Camunda Process
 * Engine, storing them in a static storage, and binding them to Micrometer registry.
 *
 * @see DdmBpmProcessEngineMetric Process engine metrics
 * @see DdmBpmBusinessProcessMetric Business-process metrics
 * @see DdmBpmSubscriptionsMetric Subscriptions metrics
 * @see DdmBpmAsyncJobsMetric Async jobs metrics
 * @see DdmBpmHistoryCleanupMetric History cleanup metrics
 */
@RequiredArgsConstructor
@Slf4j
public class CamundaMeterBinder implements MeterBinder {

  private final ProcessEngine processEngine;

  // Static storage for metric values
  private static final Map<String, Double> METRIC_VALUES = new ConcurrentHashMap<>();

  // Cache all metrics for reuse
  private final List<DdmBpmMetric> allMetrics = initializeMetrics();

  /**
   * Initializes all metrics from the various metric enums.
   */
  private List<DdmBpmMetric> initializeMetrics() {
    return Stream.of(
            DdmBpmProcessEngineMetric.values(),
            DdmBpmBusinessProcessMetric.values(),
            DdmBpmSubscriptionsMetric.values(),
            DdmBpmAsyncJobsMetric.values(),
            DdmBpmHistoryCleanupMetric.values()
        )
        .flatMap(Stream::of)
        .collect(Collectors.toList());
  }

  /**
   * Updates the value of a metric in the storage.
   *
   * @param metricName Name of the metric
   * @param value      Current value of the metric
   */
  private static void updateMetric(String metricName, double value) {
    METRIC_VALUES.put(metricName, value);
  }

  /**
   * Gets the current value of a metric from the storage.
   *
   * @param metricName Name of the metric
   * @return Current value of the metric or 0.0 if not available
   */
  private static double getMetricValue(String metricName) {
    return METRIC_VALUES.getOrDefault(metricName, 0.0);
  }

  /**
   * Binds all metrics to the provided meter registry.
   *
   * @param registry The meter registry to bind metrics to
   */
  @Override
  public void bindTo(@NonNull MeterRegistry registry) {
    // First, collect metrics immediately to have initial values
    collectMetricsNow();

    // Then register all metrics
    allMetrics.forEach(metric -> registerMetric(registry, metric));

    log.info("Bound {} Camunda metrics to registry", allMetrics.size());
  }

  /**
   * Registers a single metric with the registry.
   *
   * @param registry The meter registry
   * @param metric   The metric to register
   */
  private void registerMetric(MeterRegistry registry, DdmBpmMetric metric) {
    // Read from the static storage instead of computing on-demand
    Gauge.builder(metric.getName(), () -> getMetricValue(metric.getName()))
        .description(metric.getDescription())
        .register(registry);
  }

  /**
   * Scheduled task to collect all metrics and update the storage. This method runs every 30 seconds
   * by default.
   */
  @Scheduled(fixedDelayString = "${bpm.metrics.collection-interval-ms:30000}")
  public void collectMetrics() {
    if (log.isDebugEnabled()) {
      log.debug("Starting Camunda metrics collection...");
    }

    allMetrics.forEach(this::collectSingleMetric);
    if (log.isDebugEnabled()) {
      log.debug("Camunda metrics collection completed successfully");
    }
  }

  /**
   * Collects a single metric and stores its value.
   *
   * @param metric The metric to collect
   */
  private void collectSingleMetric(DdmBpmMetric metric) {
    try {
      double value = metric.getMetricFunction().applyAsDouble(processEngine);
      updateMetric(metric.getName(), value);

      if (log.isTraceEnabled()) {
        log.trace("Collected metric {}: {}", metric.getName(), value);
      }
    } catch (Exception e) {
      log.error("Failed to collect metric {}: {}", metric.getName(), e.getMessage(), e);
    }
  }

  /**
   * Manually trigger metrics collection. Can be used on application startup or for testing.
   */
  public void collectMetricsNow() {
    collectMetrics();
  }
}