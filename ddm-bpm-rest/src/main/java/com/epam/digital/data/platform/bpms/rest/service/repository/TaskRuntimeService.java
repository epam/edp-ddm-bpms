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

package com.epam.digital.data.platform.bpms.rest.service.repository;

import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.stereotype.Service;

/**
 * Runtime service that is used for creating task queries to Camunda {@link TaskService} and
 * {@link ProcessEngine}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskRuntimeService {

  private final TaskService taskService;
  private final RepositoryService repositoryService;
  private final ProcessEngine processEngine;
  private final ObjectMapper objectMapper;
  private final ProcessInstanceRuntimeService processInstanceRuntimeService;

  /**
   * Get task list by given parameters
   *
   * @param queryDto           the parameter query dto
   * @param paginationQueryDto query dto with pagination parameters
   * @return list of {@link TaskDto task object} that matches given params
   */
  public List<TaskDto> getTasksByParams(TaskQueryDto queryDto,
      PaginationQueryDto paginationQueryDto) {
    log.debug("Selecting tasks...");

    var result = queryDto.toQuery(processEngine)
        .listPage(paginationQueryDto.getFirstResult(), paginationQueryDto.getMaxResults())
        .stream()
        .map(TaskDto::fromEntity)
        .collect(Collectors.toList());

    log.debug("Found {} tasks", result.size());
    return result;
  }

  /**
   * Get task by id
   *
   * @param id the task id
   * @return optional value of {@link TaskDto task object}
   */
  public Optional<TaskDto> getTaskById(String id) {
    log.debug("Selecting task by id {}...", id);
    var task = taskService.createTaskQuery()
        .taskId(id)
        .initializeFormKeys()
        .list().stream()
        .map(TaskDto::fromEntity)
        .findFirst();

    log.debug("Task {} {}", id, task.isEmpty() ? "not found" : "found");
    return task;
  }

  /**
   * Get variable value map by task id
   * <p>
   * Map structure:
   * <li>key - variable name</li>
   * <li>value - {@link VariableValueDto variable value object}</li>
   *
   * @param taskId the task id
   * @return the variable value map
   */
  public Map<String, VariableValueDto> getVariables(String taskId) {
    log.debug("Selecting variables by task id {}...", taskId);
    var result = taskService.getVariables(taskId);

    log.debug("Found {} variables by task id {}...", result.size(), taskId);
    return VariableValueDto.fromMap((VariableMap) result);
  }

  /**
   * Complete task by id
   *
   * @param id  the task id
   * @param dto the dto with request variables
   * @return map with task variables that present in business process on task completion moment
   * (empty map if {@link CompleteTaskDto#isWithVariablesInReturn()} is false)
   */
  public Map<String, VariableValueDto> completeTask(String id, CompleteTaskDto dto) {
    log.debug("Completing task by id {}...", id);
    final var requestVariables = VariableValueDto.toMap(dto.getVariables(), processEngine,
        objectMapper);

    if (dto.isWithVariablesInReturn()) {
      var taskVariables = taskService.completeWithVariablesInReturn(id, requestVariables, false);
      log.debug("Task {} was completed. {} variables found", id, taskVariables.size());
      return VariableValueDto.fromMap(taskVariables, true);
    }

    taskService.complete(id, requestVariables);
    log.debug("Task {} was completed without variables in return", id);
    return Map.of();
  }

  /**
   * Returns a map containing the extended properties of the task. Key of the map is a property
   * name, Value - property value.
   * <p>
   * This method returns an empty map if the task has no properties or task with this taskId is
   * absent.
   *
   * @param taskId - task identifier
   * @return a map containing the properties of the task
   */
  public Map<String, String> getTaskProperty(String taskId) {
    log.info("Getting task {} properties", taskId);
    Map<String, String> taskProperties = new HashMap<>();
    var properties = getCamundaProperties(taskId);
    properties.forEach(pr -> taskProperties.put(pr.getCamundaName(), pr.getCamundaValue()));
    log.info("Found {} properties for task {}", taskProperties.size(), taskId);
    return taskProperties;
  }

  /**
   * Returns a list containing the CamundaProperty objects of the task.
   * <p>
   * This method returns an empty list if the task has no properties or if there is no task with
   * this taskId.
   *
   * @param taskId - task identifier
   * @return a list containing the CamundaProperty objects of the task
   */
  private List<CamundaProperty> getCamundaProperties(String taskId) {
    var task = taskService.createTaskQuery().taskId(taskId).singleResult();
    if (Objects.isNull(task)) {
      return Collections.emptyList();
    }

    return repositoryService.getBpmnModelInstance(task.getProcessDefinitionId())
        .getModelElementsByType(UserTask.class)
        .stream()
        .filter(userTask -> userTask.getId().equals(task.getTaskDefinitionKey()))
        .map(UserTask::getExtensionElements)
        .filter(Objects::nonNull)
        .flatMap(e -> e.getElementsQuery().filterByType(CamundaProperties.class).list().stream())
        .flatMap(e -> e.getCamundaProperties().stream())
        .collect(Collectors.toList());
  }
}
