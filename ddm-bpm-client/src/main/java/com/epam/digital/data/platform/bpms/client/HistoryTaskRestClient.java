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

package com.epam.digital.data.platform.bpms.client;

import com.epam.digital.data.platform.bpms.api.dto.HistoryTaskCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryTaskQueryDto;
import feign.error.ErrorHandling;
import java.util.List;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The interface extends {@link BaseFeignClient} and used to perform operations on finished camunda
 * user tasks
 */
@FeignClient(name = "history-task-client", url = "${bpms.url}/api/history/task")
public interface HistoryTaskRestClient extends BaseFeignClient {

  /**
   * Method for getting list of finished camunda user tasks
   *
   * @param historyTaskQueryDto object with search parameters
   * @return the list of finished camunda user tasks
   */
  @GetMapping
  @ErrorHandling
  List<HistoricTaskInstanceEntity> getHistoryTasksByParams(
      @SpringQueryMap HistoryTaskQueryDto historyTaskQueryDto);

  /**
   * Method for getting the number of finished camunda user tasks
   *
   * @param historyTaskCountQueryDto object with search parameters
   * @return the number of finished camunda user tasks
   */
  @PostMapping("/count")
  @ErrorHandling
  CountResultDto getHistoryTaskCountByParams(
      @RequestBody HistoryTaskCountQueryDto historyTaskCountQueryDto);
}