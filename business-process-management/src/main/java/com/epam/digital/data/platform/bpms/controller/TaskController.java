package com.epam.digital.data.platform.bpms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.rest.impl.TaskRestServiceImpl;
import org.springframework.util.CollectionUtils;

/**
 * Custom task rest service based on default camunda task rest service.
 * It allows additional logic to be added to default endpoints.
 */
public class TaskController extends TaskRestServiceImpl {

  public TaskController(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public List<TaskDto> queryTasks(TaskQueryDto queryDto, Integer firstResult,
      Integer maxResults) {
    return super.queryTasks(queryDto, firstResult, maxResults).stream()
        .filter(hasIdentityLinks())
        .collect(Collectors.toList());
  }

  private Predicate<TaskDto> hasIdentityLinks() {
    return t -> !CollectionUtils.isEmpty(
        getProcessEngine().getTaskService().getIdentityLinksForTask(t.getId()));
  }
}
