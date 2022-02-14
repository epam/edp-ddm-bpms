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

import com.epam.digital.data.platform.bpm.history.it.kafka.KafkaConsumer;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092",
    "port=9092"})
class ProcessHistoryEventPublisherIT {

  @Inject
  private RuntimeService runtimeService;
  @Inject
  private TaskService taskService;
  @Inject
  private IdentityService identityService;
  @Inject
  private HistoryService historyService;

  @Inject
  private KafkaConsumer kafkaConsumer;

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

    kafkaConsumer.consumeAll();

    var storage = kafkaConsumer.getStorage();
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
    var actualTask = storage.getHistoryTaskDtoMap().values()
        .stream()
        .findFirst();
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
  }

  @Test
  @Deployment(resources = {"bpmn/testParent.bpmn", "bpmn/testSubprocess.bpmn"})
  public void testHistoryTaskShouldHaveParentProcessDefinitionName() {
    identityService.setAuthentication("testUser", List.of("camunda-admin"));

    runtimeService.startProcessInstanceByKey("parent_process", "1",
        Map.of("initiator", "testUser"));

    var task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    kafkaConsumer.consumeAll();

    var storage = kafkaConsumer.getStorage();
    assertThat(storage.getHistoryTaskDtoMap()).hasSize(1);
    var actualTask = storage.getHistoryTaskDtoMap().values()
        .stream()
        .findFirst();
    assertThat(actualTask).isPresent().get()
        .hasNoNullFieldsOrProperties()
        .hasFieldOrPropertyWithValue("taskDefinitionKey", "sub-process-1-task")
        .hasFieldOrPropertyWithValue("processDefinitionName", "Parent process")
        .hasFieldOrPropertyWithValue("taskDefinitionName", "task sub process");

    assertThat(historyService.createHistoricTaskInstanceQuery().list()).isEmpty();
  }
}
