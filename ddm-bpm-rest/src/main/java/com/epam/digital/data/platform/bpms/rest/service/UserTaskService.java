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

package com.epam.digital.data.platform.bpms.rest.service;

import com.epam.digital.data.platform.bpms.api.dto.SignableUserTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.mapper.TaskMapper;
import com.epam.digital.data.platform.bpms.rest.service.repository.ProcessDefinitionRepositoryService;
import com.epam.digital.data.platform.bpms.rest.service.repository.TaskRuntimeService;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonation;
import com.epam.digital.data.platform.dso.api.dto.Subject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.springframework.stereotype.Service;

/**
 * The service for managing and getting user tasks extended with task properties and form variables
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserTaskService {

  private static final String SIGN_PROPERTY = "eSign";
  private static final String FORM_VARIABLES_PROPERTY = "formVariables";
  private static final String FORM_VARIABLES_REGEX = "\\s*,\\s*";

  private final ProcessDefinitionRepositoryService processDefinitionRepositoryService;
  private final TaskRuntimeService taskRuntimeService;
  private final TaskMapper taskMapper;

  @Resource(name = "camundaAdminImpersonation")
  private final CamundaImpersonation camundaAdminImpersonation;

  /**
   * Get user task by specified search parameters and pagination query parameters.
   *
   * @param taskQueryDto       object with search parameters.
   * @param paginationQueryDto object with pagination parameters.
   * @return list of {@link UserTaskDto}.
   */
  public List<UserTaskDto> getTasksByParams(TaskQueryDto taskQueryDto,
      PaginationQueryDto paginationQueryDto) {
    log.info("Getting user tasks");

    var taskDtos = taskRuntimeService.getTasksByParams(taskQueryDto, paginationQueryDto);
    log.trace("Found {} tasks", taskDtos.size());

    if (taskDtos.isEmpty()) {
      return List.of();
    }

    var processDefinitionNames = getProcessDefinitionNames(taskDtos);
    log.trace("Found process definition names - {}", processDefinitionNames.values());

    var result = taskMapper.toUserTaskDtos(taskDtos, processDefinitionNames);
    log.trace("Found user task list - {}", result);

    log.info("Found {} user tasks", result.size());
    return result;
  }

  /**
   * Get signable user task by id
   *
   * @param id task id
   * @return {@link SignableUserTaskDto}
   */
  public SignableUserTaskDto getTaskById(String id) {
    log.info("Getting user task with id {}", id);

    var taskDto = getTask(id);
    var signableUserTaskDto = taskMapper.toSignableUserTaskDto(taskDto);
    log.trace("Camunda task has been found");

    var processDefinition = getProcessDefinitionById(taskDto.getProcessDefinitionId());
    signableUserTaskDto.setProcessDefinitionName(processDefinition.getName());
    log.trace("Task was filled with process definition name");

    var properties = getTaskProperties(id);
    signableUserTaskDto.setESign(Boolean.parseBoolean(properties.get(SIGN_PROPERTY)));
    signableUserTaskDto.setFormVariables(
        getTaskFormVariables(id, properties.get(FORM_VARIABLES_PROPERTY)));
    var allowedSubjects = Arrays.stream(Subject.values())
        .filter(subject -> Boolean.parseBoolean(properties.get(subject.name())))
        .collect(Collectors.toSet());
    signableUserTaskDto.setSignatureValidationPack(allowedSubjects);
    log.trace("Task was filled with extension properties");

    log.info("User task with id {} found", id);
    return signableUserTaskDto;
  }

  private TaskDto getTask(String id) {
    var task = taskRuntimeService.getTaskById(id);
    if (task.isEmpty()) {
      throw new RestException(Status.NOT_FOUND, "No matching task with id " + id);
    }
    return task.get();
  }

  private ProcessDefinition getProcessDefinitionById(String id) {
    return camundaAdminImpersonation.execute(
        () -> processDefinitionRepositoryService.getProcessDefinitionById(id));
  }

  private Map<String, String> getTaskProperties(String id) {
    return camundaAdminImpersonation.execute(() -> taskRuntimeService.getTaskProperty(id));
  }

  private Map<String, String> getProcessDefinitionNames(List<TaskDto> taskDtos) {
    var processDefinitionIds = taskDtos.stream()
        .map(TaskDto::getProcessDefinitionId)
        .toArray(String[]::new);
    return camundaAdminImpersonation.execute(
        () -> processDefinitionRepositoryService.getProcessDefinitionsNames(processDefinitionIds));
  }

  private Map<String, Object> getTaskFormVariables(String taskId, String formVariables) {
    if (Objects.isNull(formVariables)) {
      return Map.of();
    }

    var formVariableNames = List.of(formVariables.split(FORM_VARIABLES_REGEX));
    return taskRuntimeService.getVariables(taskId).entrySet().stream()
        .filter(entry -> formVariableNames.contains(entry.getKey()) && Objects
            .nonNull(entry.getValue().getValue()))
        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getValue()));
  }
}
