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

import com.epam.digital.data.platform.bpms.api.dto.HistoryUserTaskDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceQueryDto;

/**
 * The service for managing historical user tasks.
 */
public interface HistoricTaskService {

  /**
   * Get historical user tasks by query params.
   *
   * @param queryDto           object with search parameters.
   * @param paginationQueryDto object with pagination parameters.
   * @return list of {@link HistoryUserTaskDto}
   */
  List<HistoryUserTaskDto> getHistoryUserTasksByParams(HistoricTaskInstanceQueryDto queryDto,
      PaginationQueryDto paginationQueryDto);
}
