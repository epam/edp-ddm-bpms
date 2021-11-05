package com.epam.digital.data.platform.bpms.rest.service.impl;

import com.epam.digital.data.platform.bpms.api.dto.SignableUserTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.mapper.TaskMapper;
import com.epam.digital.data.platform.bpms.rest.service.ProcessDefinitionService;
import com.epam.digital.data.platform.bpms.rest.service.TaskPropertyService;
import com.epam.digital.data.platform.bpms.rest.service.TaskService;
import com.epam.digital.data.platform.dso.api.dto.Subject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.ws.rs.core.Request;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

  private static final String SIGN_PROPERTY = "eSign";
  private static final String FORM_VARIABLES_PROPERTY = "formVariables";
  private static final String FORM_VARIABLES_REGEX = "\\s*,\\s*";

  private final ProcessDefinitionService processDefinitionService;
  private final TaskPropertyService taskPropertyService;
  private final TaskRestService taskRestService;
  private final TaskMapper taskMapper;

  @Override
  public List<UserTaskDto> getTasksByParams(TaskQueryDto taskQueryDto,
      PaginationQueryDto paginationQueryDto) {
    log.debug("Getting user tasks");
    var taskDtos = taskRestService.queryTasks(taskQueryDto, paginationQueryDto.getFirstResult(),
        paginationQueryDto.getMaxResults());

    var processDefinitionIds = taskDtos.stream().map(TaskDto::getProcessDefinitionId)
        .collect(Collectors.toList());
    log.trace("Found {} process definition ids from task list. Result - {}",
        processDefinitionIds.size(), processDefinitionIds);
    var processDefinitionsIdAndNameMap = processDefinitionService.getProcessDefinitionsNames(
        processDefinitionIds);
    log.trace("Found process definition names - {}", processDefinitionsIdAndNameMap.values());

    var result = taskDtos.stream().map(t -> toUserTaskDto(t, processDefinitionsIdAndNameMap))
        .collect(Collectors.toList());
    log.info("Found {} user tasks", result.size());
    log.debug("Found user task list - {}", result);
    return result;
  }

  @Override
  public SignableUserTaskDto getTaskById(String id, Request context) {
    log.info("Getting user task with id {}", id);

    var taskDto = (TaskDto) taskRestService.getTask(id).getTask(context);
    var signableUserTaskDto = taskMapper.toSignableUserTaskDto(taskDto);
    log.trace("Camunda task has been found");

    var processDefinition = processDefinitionService
        .getProcessDefinition(taskDto.getProcessDefinitionId());
    signableUserTaskDto.setProcessDefinitionName(processDefinition.getName());
    log.trace("Task was filled with process definition name");

    var properties = taskPropertyService.getTaskProperty(id);
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

  private UserTaskDto toUserTaskDto(TaskDto taskDto,
      Map<String, String> processDefinitionsIdAndNameMap) {
    var userTask = taskMapper.toUserTaskDto(taskDto);
    userTask.setProcessDefinitionName(
        processDefinitionsIdAndNameMap.get(userTask.getProcessDefinitionId()));
    return userTask;
  }

  private Map<String, Object> getTaskFormVariables(String taskId, String formVariables) {
    if (Objects.isNull(formVariables)) {
      return Map.of();
    }

    var formVariableNames = List.of(formVariables.split(FORM_VARIABLES_REGEX));
    return taskRestService.getTask(taskId).getVariables().getVariables(true).entrySet().stream()
        .filter(entry -> formVariableNames.contains(entry.getKey()))
        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getValue()));
  }
}
