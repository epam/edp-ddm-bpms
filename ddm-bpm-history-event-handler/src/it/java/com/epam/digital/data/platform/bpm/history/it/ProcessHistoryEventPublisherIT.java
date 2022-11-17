/*
 * Copyright 2021 EPAM Systems.
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

package com.epam.digital.data.platform.bpm.history.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertThrows;

import com.epam.digital.data.platform.bphistory.model.HistoryProcess;
import com.epam.digital.data.platform.bphistory.model.HistoryTask;
import com.epam.digital.data.platform.bpm.history.it.storage.TestHistoryEventStorage;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.awaitility.Awaitility;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(count = 3)
class ProcessHistoryEventPublisherIT {

  @Inject
  private RuntimeService runtimeService;
  @Inject
  private TaskService taskService;
  @Inject
  private IdentityService identityService;
  @Inject
  private HistoryService historyService;

  @Autowired
  private EmbeddedKafkaBroker embeddedKafkaBroker;
  @Autowired
  private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
  private static TestHistoryEventStorage storage;

  @BeforeEach
  void setUp() {
    kafkaListenerEndpointRegistry.getListenerContainers().forEach(container ->
        ContainerTestUtils.waitForAssignment(container,
            embeddedKafkaBroker.getPartitionsPerTopic()));
    storage = new TestHistoryEventStorage();
  }

  @Component
  static class ProcessListener {

    @KafkaListener(
        topics = "bpm-history-process",
        groupId = "process-history-api",
        containerFactory = "concurrentKafkaListenerContainerFactory", autoStartup = "true")
    public void receive(HistoryProcess input) {
      if (Objects.isNull(storage.getHistoryProcessInstanceDto(input.getProcessInstanceId()))) {
        storage.put(input);
      } else {
        storage.patch(input);
      }
    }
  }

  @Component
  static class TaskListener {

    @KafkaListener(
        topics = "bpm-history-task",
        groupId = "process-history-api",
        containerFactory = "concurrentKafkaListenerContainerFactory", autoStartup = "true")
    public void receive(HistoryTask input) {
      if (Objects.isNull(storage.getHistoryTaskDto(input.getActivityInstanceId()))) {
        storage.put(input);
      } else {
        storage.patch(input);
      }
    }
  }

  @Test
  @Deployment(resources = "bpmn/testHistoryHandler.bpmn")
  void testInMemHistoryHandler() {
    identityService.setAuthentication("testUser", List.of("camunda-admin"));

    var process = runtimeService.startProcessInstanceByKey("test_history_handler",
        "1", Map.of("initiator", "testUser"));

    var task = taskService.createTaskQuery()
        .processInstanceId(process.getProcessInstanceId())
        .singleResult();
    taskService.complete(task.getId());

    var processInstanceId = process.getId();
    var processDefinitionId = process.getProcessDefinitionId();

    await().atMost(10, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS).untilAsserted(() -> {
      assertThat(storage.getProcessInstanceDtoMap()).hasSize(1);
      var actualProcessInstance = storage.getHistoryProcessInstanceDto(processInstanceId);
      assertThat(actualProcessInstance)
          .hasNoNullFieldsOrPropertiesExcept("superProcessInstanceId")
          .hasFieldOrPropertyWithValue("processInstanceId", processInstanceId)
          .hasFieldOrPropertyWithValue("superProcessInstanceId", null)
          .hasFieldOrPropertyWithValue("processDefinitionId", processDefinitionId)
          .hasFieldOrPropertyWithValue("processDefinitionKey", "test_history_handler")
          .hasFieldOrPropertyWithValue("processDefinitionName", "test_history_handler")
          .hasFieldOrPropertyWithValue("businessKey", "1")
          .hasFieldOrProperty("startTime")
          .hasFieldOrProperty("endTime")
          .hasFieldOrPropertyWithValue("startUserId", "testUser")
          .hasFieldOrPropertyWithValue("state", "COMPLETED")
          .hasFieldOrPropertyWithValue("excerptId", "excerpt")
          .hasFieldOrPropertyWithValue("completionResult", "completion result");

      assertThat(storage.getHistoryTaskDtoMap()).hasSize(1);
      var actualTask = storage.getHistoryTaskDtoMap().values().stream()
          .filter(t -> "user_task".equals(t.getTaskDefinitionKey())).findFirst();
      assertThat(actualTask).isPresent().get()
          .hasNoNullFieldsOrProperties()
          .hasFieldOrProperty("activityInstanceId")
          .hasFieldOrPropertyWithValue("taskDefinitionKey", "user_task")
          .hasFieldOrPropertyWithValue("taskDefinitionName", "task")
          .hasFieldOrPropertyWithValue("processInstanceId", processInstanceId)
          .hasFieldOrPropertyWithValue("processDefinitionId", processDefinitionId)
          .hasFieldOrPropertyWithValue("processDefinitionKey", "test_history_handler")
          .hasFieldOrPropertyWithValue("processDefinitionName", "test_history_handler")
          .hasFieldOrPropertyWithValue("rootProcessInstanceId", processInstanceId)
          .hasFieldOrProperty("startTime")
          .hasFieldOrProperty("endTime")
          .hasFieldOrPropertyWithValue("assignee", "testUser");

      assertThat(historyService.createHistoricProcessInstanceQuery().list()).isEmpty();
      assertThat(historyService.createHistoricTaskInstanceQuery().list()).isEmpty();
    });
  }

  @Test
  @Deployment(resources = {"bpmn/testParent.bpmn", "bpmn/testSubprocess.bpmn"})
  public void testHistoryTaskShouldHaveParentProcessDefinitionName() {
    identityService.setAuthentication("testUser", List.of("camunda-admin"));

    runtimeService.startProcessInstanceByKey("parent_process", "1",
        Map.of("initiator", "testUser"));

    var task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    await().atMost(10, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS).untilAsserted(() -> {
      assertThat(storage.getHistoryTaskDtoMap()).hasSize(1);
      var actualTask = storage.getHistoryTaskDtoMap().values().stream()
          .filter(t -> "sub-process-1-task".equals(t.getTaskDefinitionKey())).findFirst();
      assertThat(actualTask).isPresent().get()
          .hasNoNullFieldsOrProperties()
          .hasFieldOrPropertyWithValue("taskDefinitionKey", "sub-process-1-task")
          .hasFieldOrPropertyWithValue("processDefinitionName", "Parent process")
          .hasFieldOrPropertyWithValue("taskDefinitionName", "task sub process");

      assertThat(historyService.createHistoricTaskInstanceQuery().list()).isEmpty();
    });
  }

  @Test
  @Deployment(resources = "bpmn/testKafkaRollback.bpmn")
  void testKafkaRollback() {
    identityService.setAuthentication("testUser", List.of("camunda-admin"));
    assertThrows(ValidationException.class,
        () -> runtimeService.startProcessInstanceByKey("test_kafka_rollback",
            "1", Map.of("initiator", "testUser")));

    Awaitility.await().pollDelay(4, TimeUnit.SECONDS).untilAsserted(() -> {
      assertThat(storage.getHistoryTaskDtoMap().size()).isZero();
      assertThat(storage.getProcessInstanceDtoMap().size()).isZero();
    });
  }
}
