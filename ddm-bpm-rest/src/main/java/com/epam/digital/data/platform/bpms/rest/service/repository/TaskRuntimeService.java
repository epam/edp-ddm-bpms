package com.epam.digital.data.platform.bpms.rest.service.repository;

import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
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
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.stereotype.Service;

/**
 * Runtime service that is used for creating task queries to Camunda {@link TaskService} and {@link
 * ProcessEngine}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskRuntimeService {

  private final TaskService taskService;
  private final RepositoryService repositoryService;
  private final ProcessEngine processEngine;

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
