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

package com.epam.digital.data.platform.bpm.it.util;

import static java.util.stream.Collectors.toMap;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.processDefinition;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;

import com.epam.digital.data.platform.bpm.it.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

@Slf4j
public final class CamundaAssertionUtil {

  private static final ThreadLocal<FormDataStorageService> fromDataStorageService = new ThreadLocal<>();

  private CamundaAssertionUtil() {
  }

  public static FormDataStorageService formDataStorageService() {
    return fromDataStorageService.get();
  }

  public static void setFromDataStorageService(FormDataStorageService formDataStorageService) {
    fromDataStorageService.set(formDataStorageService);
  }

  public static ProcessInstance processInstance(String processInstanceId) {
    return BpmnAwareTests.processInstanceQuery()
        .processInstanceId(processInstanceId)
        .singleResult();
  }

  public static void assertWaitingActivity(AssertWaitingActivityDto assertWaitingActivityDto) {
    var processInstanceId = assertWaitingActivityDto.getProcessInstanceId();
    var activityDefinitionId = assertWaitingActivityDto.getActivityDefinitionId();

    assertThat(processInstance(processInstanceId)).isWaitingAt(activityDefinitionId);

    var task = task(activityDefinitionId);
    if (Objects.nonNull(task)) {
      assertUserTask(assertWaitingActivityDto, task);
    }

    var expectedFormData = assertWaitingActivityDto.getExpectedFormDataPrePopulation();
    if (Objects.nonNull(expectedFormData)) {
      var cephKey = generateCephKey(activityDefinitionId, processInstanceId);
      assertCephContains(cephKey, expectedFormData);
    }

    var expectedVariables = assertWaitingActivityDto.getExpectedVariables();
    if (!expectedVariables.isEmpty()) {
      assertThat(processInstance(processInstanceId)).variables()
          .containsAllEntriesOf(expectedVariables);
    }
  }

  public static String generateCephKey(String activityDefinitionId, String processInstanceId) {
    return String.format("process/%s/task/%s", processInstanceId, activityDefinitionId);
  }

  public static void assertCephContains(String cephKey, FormDataDto cephContent) {
    Assertions.assertThat(formDataStorageService().getFormData(cephKey)).get()
        .isEqualTo(cephContent);
  }

  private static void assertUserTask(AssertWaitingActivityDto assertWaitingActivityDto,
      Task task) {
    var processDefinitionId = assertWaitingActivityDto.getProcessDefinitionKey();
    var activityDefinitionId = assertWaitingActivityDto.getActivityDefinitionId();

    assertThat(task).hasFormKey(assertWaitingActivityDto.getFormKey());

    if (Objects.nonNull(assertWaitingActivityDto.getAssignee())) {
      assertThat(task).isAssignedTo(assertWaitingActivityDto.getAssignee());
    } else if (!assertWaitingActivityDto.getCandidateUsers().isEmpty()) {
      assertWaitingActivityDto.getCandidateUsers()
          .forEach(candidateUser -> assertThat(task).hasCandidateUser(candidateUser));
    } else if (!assertWaitingActivityDto.getCandidateRoles().isEmpty()) {
      assertWaitingActivityDto.getCandidateRoles()
          .forEach(candidateRole -> assertThat(task).hasCandidateGroup(candidateRole));
    } else {
      log.warn("User task's ({}) assignee in process {} wasn't asserted", activityDefinitionId,
          processDefinitionId);
    }

    var extensionElements = getExtensionElements(processDefinitionId, activityDefinitionId);
    if (!assertWaitingActivityDto.getExtensionElements().isEmpty()) {
      Assertions.assertThat(extensionElements)
          .containsAllEntriesOf(assertWaitingActivityDto.getExtensionElements());
    }
  }

  private static Map<String, String> getExtensionElements(String processDefinitionKey,
      String activityDefinitionId) {
    var taskDefinition = BpmnAwareTests.repositoryService()
        .getBpmnModelInstance(processDefinition(processDefinitionKey).getId())
        .getModelElementsByType(UserTask.class)
        .stream()
        .filter(userTask -> userTask.getId().equals(activityDefinitionId))
        .findFirst();
    Assertions.assertThat(taskDefinition).isNotEmpty();
    return taskDefinition.map(UserTask::getExtensionElements)
        .map(ExtensionElements::getElementsQuery)
        .map(query -> query.filterByType(CamundaProperties.class).list())
        .orElse(Collections.emptyList()).stream()
        .flatMap(camundaProperties -> camundaProperties.getCamundaProperties().stream())
        .filter(camundaProperty -> Objects.nonNull(camundaProperty.getCamundaValue()))
        .collect(toMap(CamundaProperty::getCamundaName, CamundaProperty::getCamundaValue));
  }
}
