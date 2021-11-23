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
