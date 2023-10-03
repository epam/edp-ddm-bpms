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

package com.epam.digital.data.platform.bpm.metrics;

import com.epam.digital.data.platform.bpm.metrics.binder.CamundaMeterBinder;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import io.zonky.test.db.postgres.embedded.LiquibasePreparer;
import lombok.SneakyThrows;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
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

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Stream;

import static com.epam.digital.data.platform.bpm.metrics.DdmBpmAsyncJobsMetric.*;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmBusinessProcessMetric.*;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmHistoryCleanupMetric.REMOVED_PROCESS_INSTANCES_METRICS;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmHistoryCleanupMetric.REMOVED_TASKS_METRICS;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmProcessEngineMetric.*;
import static com.epam.digital.data.platform.bpm.metrics.DdmBpmSubscriptionsMetric.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = Application.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY)
@AutoConfigureMockMvc
@AutoConfigureMetrics
@ExtendWith(DdmBpmMetricTest.LiquibasePreparationExtension.class)
class DdmBpmMetricTest {

  @Autowired
  MockMvc mockMvc;

  @ParameterizedTest
  @MethodSource("provideParamsForMetrics")
  @SneakyThrows
  void testMetricGathering(DdmBpmMetric metric, double expectedMetricNumber) {
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
        Arguments.of(USER_COUNT_METRIC, 3D),
        Arguments.of(AUTHORIZATION_COUNT_METRIC, 4D),
        Arguments.of(DEPLOYMENTS_METRIC, 5D),
        Arguments.of(ACTIVE_PROCESS_DEFINITIONS_METRIC, 4D),
        Arguments.of(ACTIVE_USER_TASKS_METRIC, 9D),
        Arguments.of(ACTIVE_USER_TASKS_ASSIGNED_METRIC, 5D),
        Arguments.of(ACTIVE_USER_TASKS_UNASSIGNED_METRIC, 4D),
        Arguments.of(COMPLETED_ROOT_PROCESS_INSTANCES_METRIC, 3D),
        Arguments.of(TERMINATED_ROOT_PROCESS_INSTANCES_METRIC, 2D),
        Arguments.of(SUSPENDED_ROOT_PROCESS_INSTANCES_METRIC, 2D),
        Arguments.of(ACTIVE_ROOT_PROCESS_INSTANCES_METRIC, 1D),
        Arguments.of(ROOT_PROCESS_INSTANCES_METRIC, 9D),
        Arguments.of(ACTIVE_INCIDENTS_METRIC, 6D),
        Arguments.of(ACTIVE_SIGNAL_EVENT_SUBSCRIPTIONS_METRIC, 1D),
        Arguments.of(ACTIVE_CONDITIONAL_EVENT_SUBSCRIPTIONS_METRIC, 2D),
        Arguments.of(ACTIVE_COMPENSATE_EVENT_SUBSCRIPTIONS_METRIC, 3D),
        Arguments.of(ACTIVE_MESSAGE_EVENT_SUBSCRIPTIONS_METRIC, 4D),
        Arguments.of(MESSAGE_JOBS_METRIC, 3D),
        Arguments.of(TIMER_JOBS_METRIC, 2D),
        Arguments.of(EXECUTABLE_TIMER_JOBS_METRIC, 1D),
        Arguments.of(EXECUTABLE_JOBS_METRIC, 4D),
        Arguments.of(REMOVED_PROCESS_INSTANCES_METRICS, 702D),
        Arguments.of(REMOVED_TASKS_METRICS, 2792D)
    );
  }

  static class LiquibasePreparationExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
      var appContext = SpringExtension.getApplicationContext(context);
      var dataSource = appContext.getBean(DataSource.class);
      var changelogList = List.of(
          "liquibase/active-incidents-metric.sql",
          "liquibase/active-procdef-metric.sql",
          "liquibase/async-jobs-metrics.sql",
          "liquibase/auth-count-metric.sql",
          "liquibase/deployments-metric.sql",
          "liquibase/history-cleanup-metric.sql",
          "liquibase/process-instance-metrics.sql",
          "liquibase/subscription-metrics.sql",
          "liquibase/user-count-metric.sql",
          "liquibase/user-tasks-metrics.sql"
      );
      for (var changelog : changelogList) {
        LiquibasePreparer.forClasspathLocation(changelog).prepare(dataSource);
      }

      var camundaMeterBinder = appContext.getBean(CamundaMeterBinder.class);
      camundaMeterBinder.collectMetricsNow();
    }
  }
}
