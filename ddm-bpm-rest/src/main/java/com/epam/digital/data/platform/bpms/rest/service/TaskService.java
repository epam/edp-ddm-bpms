package com.epam.digital.data.platform.bpms.rest.service;

import com.epam.digital.data.platform.bpms.api.dto.SignableUserTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import java.util.List;
import javax.ws.rs.core.Request;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;

/**
 * The service for managing and getting user tasks.
 */
public interface TaskService {

  /**
   * Get user task by specified search parameters and pagination query parameters.
   *
   * @param taskQueryDto       object with search parameters.
   * @param paginationQueryDto object with pagination parameters.
   * @return list of {@link UserTaskDto}.
   */
  List<UserTaskDto> getTasksByParams(TaskQueryDto taskQueryDto,
      PaginationQueryDto paginationQueryDto);

  /**
   * Get signable user task by id
   *
   * @param id      task id
   * @param context {@link Request} object that represents context of current request
   * @return {@link SignableUserTaskDto}
   */
  SignableUserTaskDto getTaskById(String id, Request context);

}
