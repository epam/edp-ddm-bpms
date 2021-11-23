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

package com.epam.digital.data.platform.bpms.rest.controller;

import com.epam.digital.data.platform.bpms.api.dto.SignableUserTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.service.TaskService;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
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

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public SignableUserTaskDto getById(@PathParam("id") String id, @Context Request context) {
    return taskService.getTaskById(id, context);
  }
}
