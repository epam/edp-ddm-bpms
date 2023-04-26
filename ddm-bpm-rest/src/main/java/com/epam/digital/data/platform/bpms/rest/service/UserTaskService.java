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

package com.epam.digital.data.platform.bpms.rest.service;

import com.epam.digital.data.platform.bpms.api.dto.DdmCompletedTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmLightweightTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmSignableTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmTaskQueryDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.mapper.TaskMapper;
import com.epam.digital.data.platform.bpms.rest.service.repository.ProcessDefinitionRepositoryService;
import com.epam.digital.data.platform.bpms.rest.service.repository.ProcessInstanceRuntimeService;
import com.epam.digital.data.platform.bpms.rest.service.repository.TaskRuntimeService;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonation;
import com.epam.digital.data.platform.dso.api.dto.Subject;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.runtime.ProcessInstance;
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
  private final ProcessInstanceRuntimeService processInstanceRuntimeService;
  private final TaskRuntimeService taskRuntimeService;

  private final TaskMapper taskMapper;

  @Resource(name = "camundaAdminImpersonation")
  private final CamundaImpersonation camundaAdminImpersonation;

  /**
   * Get user task by specified search parameters and pagination query parameters.
   *
   * @param taskQueryDto       object with search parameters.
   * @param paginationQueryDto object with pagination parameters.
   * @return list of {@link DdmTaskDto}.
   */
  public List<DdmTaskDto> getTasksByParams(TaskQueryDto taskQueryDto,
      PaginationQueryDto paginationQueryDto) {
    log.info("Getting user tasks");

    var taskDtos = taskRuntimeService.getTasksByParams(taskQueryDto, paginationQueryDto);
    log.trace("Found {} tasks", taskDtos.size());

    if (taskDtos.isEmpty()) {
      return List.of();
    }

    var processInstances = getProcessInstances(taskDtos);

    var processDefinitionNames = getProcessDefinitionNames(taskDtos, processInstances);
    log.trace("Found process definition names - {}", processDefinitionNames.values());

    var processBusinessKeys = getProcessBusinessKeys(processInstances.values());
    log.trace("Found {} process business keys", processBusinessKeys.size());

    var result = taskMapper.toDdmTaskDtos(taskDtos, processDefinitionNames,
        processBusinessKeys);
    log.trace("Found user task list - {}", result);

    log.info("Found {} user tasks", result.size());
    return result;
  }

  /**
   * Get lightweight user task by specified search parameters and pagination query parameters.
   *
   * @param ddmTaskQueryDto    object with search parameters.
   * @param paginationQueryDto object with pagination parameters.
   * @return list of {@link DdmLightweightTaskDto}.
   */
  public List<DdmLightweightTaskDto> getLightweightTasksByParam(DdmTaskQueryDto ddmTaskQueryDto,
      PaginationQueryDto paginationQueryDto) {
    log.info("Getting lightweight user tasks");

    var taskDtos = getTasksByParamsIncludeCallActivities(ddmTaskQueryDto, paginationQueryDto);
    log.trace("Found {} tasks", taskDtos.size());

    if (taskDtos.isEmpty()) {
      return List.of();
    }

    var result = taskMapper.toDdmLightweightTaskDtoList(taskDtos);
    log.trace("Found user task list - {}", result);

    log.info("Found {} user tasks", result.size());
    return result;
  }

  private List<TaskDto> getTasksByParamsIncludeCallActivities(DdmTaskQueryDto ddmTaskQueryDto,
      PaginationQueryDto paginationQueryDto) {
    var rootProcessInstanceId = ddmTaskQueryDto.getRootProcessInstanceId();
    if (Objects.nonNull(rootProcessInstanceId)) {
      var callActivityProcessInstances = getCallActivityProcessInstances(rootProcessInstanceId);
      var processInstanceIds = callActivityProcessInstances.stream()
          .map(ProcessInstance::getId)
          .collect(Collectors.toList());
      processInstanceIds.add(rootProcessInstanceId);
      ddmTaskQueryDto.setProcessInstanceIdIn(processInstanceIds);
    }
    var taskQueryDto = taskMapper.toTaskQueryDto(ddmTaskQueryDto);
    return taskRuntimeService.getTasksByParams(taskQueryDto, paginationQueryDto);
  }

  /**
   * Get signable user task by id
   *
   * @param id task id
   * @return {@link DdmSignableTaskDto}
   */
  public DdmSignableTaskDto getTaskById(String id) {
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

  /**
   * Complete user task by id and check if root process instance id is ended
   *
   * @param id  the task id
   * @param dto dto with request variables
   * @return {@link DdmCompletedTaskDto}
   * @throws RestException           in case of any process engine exception
   * @throws InvalidRequestException in case of any rest exception
   */
  public DdmCompletedTaskDto completeTask(String id, CompleteTaskDto dto) {
    log.info("Completing user task with id {}", id);

    var processInstanceId = getTask(id).getProcessInstanceId();
    log.trace("Found user task process-instance id {}", processInstanceId);

    var processInstance = getProcessInstance(processInstanceId)
        .orElseThrow(() -> {
          var message = String.format("Process instance %s is missed before task %s completion",
              processInstanceId, id);
          return new IllegalStateException(message);
        });
    var rootProcessInstanceId = getRootProcessInstance(processInstance).getId();
    log.trace("Found user task root process-instance id {}", rootProcessInstanceId);

    var responseVariables = completeRuntimeTask(id, dto);
    log.trace("Task completed. Returned {} variables", responseVariables.size());

    var rootProcessInstanceEnded = getProcessInstance(rootProcessInstanceId).isEmpty();
    log.trace("Checked if root process instance {} is completed.", rootProcessInstanceId);

    log.info("User task {} was completed.", id);
    return DdmCompletedTaskDto.builder()
        .id(id)
        .processInstanceId(processInstanceId)
        .rootProcessInstanceId(rootProcessInstanceId)
        .rootProcessInstanceEnded(rootProcessInstanceEnded)
        .variables(taskMapper.toDdmVariableValueDtoMap(responseVariables))
        .build();
  }

  private Optional<ProcessInstance> getProcessInstance(String processInstanceId) {
    return camundaAdminImpersonation.execute(
        () -> processInstanceRuntimeService.getProcessInstance(processInstanceId));
  }

  private List<ProcessInstance> getCallActivityProcessInstances(String rootProcessInstanceId) {
    return camundaAdminImpersonation.execute(
        () -> processInstanceRuntimeService.getCallActivityProcessInstances(rootProcessInstanceId));
  }

  private Map<String, VariableValueDto> completeRuntimeTask(String id, CompleteTaskDto dto) {
    try {
      return taskRuntimeService.completeTask(id, dto);
    } catch (RestException e) {
      String errorMessage = String.format("Cannot complete task %s: %s", id, e.getMessage());
      throw new InvalidRequestException(e.getStatus(), e, errorMessage);
    } catch (ProcessEngineException e) {
      String errorMessage = String.format("Cannot complete task %s: %s", id, e.getMessage());
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, errorMessage);
    }
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

  private Map<String, String> getProcessDefinitionNames(List<TaskDto> taskDtos,
      Map<String, ProcessInstance> processInstances) {
    var taskIdAndRootProcessInstance =
        taskDtos.stream().collect(Collectors.toMap(TaskDto::getId,
            t -> getRootProcessInstance(processInstances.get(t.getProcessInstanceId()))));

    var taskIdAndProcessDefinitionId = taskIdAndRootProcessInstance.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getProcessDefinitionId()));

    var processDefinitionsNames = getProcessDefinitionNames(
        taskIdAndProcessDefinitionId.values().toArray(String[]::new));

    return taskIdAndProcessDefinitionId.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, e -> processDefinitionsNames.get(e.getValue())));
  }

  private Map<String, ProcessInstance> getProcessInstances(List<TaskDto> taskDtos) {
    var processInstanceIds = taskDtos.stream()
        .map(TaskDto::getProcessInstanceId)
        .collect(Collectors.toSet());
    var queryDto = new ProcessInstanceQueryDto();
    queryDto.setProcessInstanceIds(processInstanceIds);
    return camundaAdminImpersonation.execute(
        () -> processInstanceRuntimeService.getProcessInstances(queryDto,
                PaginationQueryDto.builder().build())
            .stream().collect(Collectors.toMap(ProcessInstance::getId, Function.identity())));
  }

  private ProcessInstance getRootProcessInstance(ProcessInstance processInstance) {
    return camundaAdminImpersonation.execute(
        () -> processInstanceRuntimeService.getRootProcessInstance(processInstance));
  }

  private Map<String, String> getProcessDefinitionNames(String... processDefinitionIds) {
    return camundaAdminImpersonation.execute(
        () -> processDefinitionRepositoryService.getProcessDefinitionsNames(processDefinitionIds));
  }

  private Map<String, String> getProcessBusinessKeys(Collection<ProcessInstance> processInstances) {
    return processInstances.stream()
        .filter(processInstanceDto -> Objects.nonNull(processInstanceDto.getBusinessKey()))
        .collect(Collectors.toMap(ProcessInstance::getId, ProcessInstance::getBusinessKey));
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
