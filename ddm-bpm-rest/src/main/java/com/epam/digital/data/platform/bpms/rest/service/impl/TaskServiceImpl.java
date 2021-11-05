package com.epam.digital.data.platform.bpms.rest.service.impl;

import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.mapper.TaskMapper;
import com.epam.digital.data.platform.bpms.rest.service.ProcessDefinitionService;
import com.epam.digital.data.platform.bpms.rest.service.TaskService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

  private final ProcessDefinitionService processDefinitionService;
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

  private UserTaskDto toUserTaskDto(TaskDto taskDto,
      Map<String, String> processDefinitionsIdAndNameMap) {
    var userTask = taskMapper.toUserTaskDto(taskDto);
    userTask.setProcessDefinitionName(
        processDefinitionsIdAndNameMap.get(userTask.getProcessDefinitionId()));
    return userTask;
  }
}
