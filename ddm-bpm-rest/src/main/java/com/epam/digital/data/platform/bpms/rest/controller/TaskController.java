package com.epam.digital.data.platform.bpms.rest.controller;

import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.service.TaskService;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.springframework.stereotype.Component;

/**
 * The controller that contains additional and extended endpoints for managing and getting user
 * tasks.
 */
@Component
@RequiredArgsConstructor
@Path("/extended/task")
public class TaskController {

  private final TaskService taskService;

  /**
   * Get list of user tasks by provided query params.
   *
   * @param taskQueryDto       contains query params.
   * @param paginationQueryDto specified pagination.
   * @return list of {@link UserTaskDto}
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public List<UserTaskDto> getByParams(TaskQueryDto taskQueryDto,
      @BeanParam PaginationQueryDto paginationQueryDto) {
    return taskService.getTasksByParams(taskQueryDto, paginationQueryDto);
  }
}
