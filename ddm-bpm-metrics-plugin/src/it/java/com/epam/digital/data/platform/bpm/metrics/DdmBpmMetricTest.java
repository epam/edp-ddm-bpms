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

import static com.epam.digital.data.platform.bpm.metrics.DdmBpmAsyncJobsMetric.EXECUTABLE_JOBS_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmAsyncJobsMetric.EXECUTABLE_TIMER_JOBS_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmAsyncJobsMetric.MESSAGE_JOBS_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmAsyncJobsMetric.TIMER_JOBS_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmBusinessProcessMetric.ACTIVE_INCIDENTS_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmBusinessProcessMetric.ACTIVE_ROOT_PROCESS_INSTANCES_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmBusinessProcessMetric.ACTIVE_USER_TASKS_ASSIGNED_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmBusinessProcessMetric.ACTIVE_USER_TASKS_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmBusinessProcessMetric.ACTIVE_USER_TASKS_UNASSIGNED_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmBusinessProcessMetric.COMPLETED_ROOT_PROCESS_INSTANCES_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmBusinessProcessMetric.ROOT_PROCESS_INSTANCES_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmBusinessProcessMetric.SUSPENDED_ROOT_PROCESS_INSTANCES_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmBusinessProcessMetric.TERMINATED_ROOT_PROCESS_INSTANCES_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmHistoryCleanupMetric.REMOVED_PROCESS_INSTANCES_METRICS;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmHistoryCleanupMetric.REMOVED_TASKS_METRICS;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmProcessEngineMetric.ACTIVE_PROCESS_DEFINITIONS_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmProcessEngineMetric.AUTHORIZATION_COUNT_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmProcessEngineMetric.DEPLOYMENTS_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmProcessEngineMetric.USER_COUNT_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmSubscriptionsMetric.ACTIVE_COMPENSATE_EVENT_SUBSCRIPTIONS_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmSubscriptionsMetric.ACTIVE_CONDITIONAL_EVENT_SUBSCRIPTIONS_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmSubscriptionsMetric.ACTIVE_MESSAGE_EVENT_SUBSCRIPTIONS_METRIC;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmSubscriptionsMetric.ACTIVE_SIGNAL_EVENT_SUBSCRIPTIONS_METRIC;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import io.zonky.test.db.postgres.embedded.LiquibasePreparer;
import java.util.stream.Stream;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = Application.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY)
@AutoConfigureMockMvc
@AutoConfigureMetrics
class DdmBpmMetricTest {

  @Autowired
  MockMvc mockMvc;
  @Autowired
  private DataSource dataSource;

  @ParameterizedTest
  @MethodSource("provideParamsForMetrics")
  @SneakyThrows
  void testMetricGathering(String sqlScript, DdmBpmMetric metric, double expectedMetricNumber) {
    LiquibasePreparer.forClasspathLocation("liquibase/" + sqlScript).prepare(dataSource);

    final var metricName = metric.getName().replace(".", "_");
    final var metricDescription = metric.getDescription();

    final var expectedContent = String.format("# HELP %s %s\n# TYPE %s gauge\n%s %.1f",
        metricName, metricDescription, metricName, metricName, expectedMetricNumber);

    mockMvc.perform(
        get("/actuator/prometheus")
    ).andExpectAll(
        status().isOk(),
        content().string(new StringContains(expectedContent))
    );
  }

  private static Stream<Arguments> provideParamsForMetrics() {
    return Stream.of(
        Arguments.of("user-count-metric.sql", USER_COUNT_METRIC, 3D),
        Arguments.of("auth-count-metric.sql", AUTHORIZATION_COUNT_METRIC, 4D),
        Arguments.of("deployments-metric.sql", DEPLOYMENTS_METRIC, 5D),
        Arguments.of("active-procdef-metric.sql", ACTIVE_PROCESS_DEFINITIONS_METRIC, 4D),
        Arguments.of("user-tasks-metrics.sql", ACTIVE_USER_TASKS_METRIC, 9D),
        Arguments.of("user-tasks-metrics.sql", ACTIVE_USER_TASKS_ASSIGNED_METRIC, 5D),
        Arguments.of("user-tasks-metrics.sql", ACTIVE_USER_TASKS_UNASSIGNED_METRIC, 4D),
        Arguments.of("process-instance-metrics.sql", COMPLETED_ROOT_PROCESS_INSTANCES_METRIC, 3D),
        Arguments.of("process-instance-metrics.sql", TERMINATED_ROOT_PROCESS_INSTANCES_METRIC, 2D),
        Arguments.of("process-instance-metrics.sql", SUSPENDED_ROOT_PROCESS_INSTANCES_METRIC, 2D),
        Arguments.of("process-instance-metrics.sql", ACTIVE_ROOT_PROCESS_INSTANCES_METRIC, 1D),
        Arguments.of("process-instance-metrics.sql", ROOT_PROCESS_INSTANCES_METRIC, 9D),
        Arguments.of("active-incidents-metric.sql", ACTIVE_INCIDENTS_METRIC, 6D),
        Arguments.of("subscription-metrics.sql", ACTIVE_SIGNAL_EVENT_SUBSCRIPTIONS_METRIC, 1D),
        Arguments.of("subscription-metrics.sql", ACTIVE_CONDITIONAL_EVENT_SUBSCRIPTIONS_METRIC, 2D),
        Arguments.of("subscription-metrics.sql", ACTIVE_COMPENSATE_EVENT_SUBSCRIPTIONS_METRIC, 3D),
        Arguments.of("subscription-metrics.sql", ACTIVE_MESSAGE_EVENT_SUBSCRIPTIONS_METRIC, 4D),
        Arguments.of("async-jobs-metrics.sql", MESSAGE_JOBS_METRIC, 3D),
        Arguments.of("async-jobs-metrics.sql", TIMER_JOBS_METRIC, 2D),
        Arguments.of("async-jobs-metrics.sql", EXECUTABLE_TIMER_JOBS_METRIC, 1D),
        Arguments.of("async-jobs-metrics.sql", EXECUTABLE_JOBS_METRIC, 4D),
        Arguments.of("history-cleanup-metric.sql", REMOVED_PROCESS_INSTANCES_METRICS, 702D),
        Arguments.of("history-cleanup-metric.sql", REMOVED_TASKS_METRICS, 2792D)
    );
  }
}
