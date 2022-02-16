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

import com.epam.digital.data.platform.bpms.api.dto.DdmClaimTaskQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmCompleteTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmCompletedTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmCountResultDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmLightweightTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmSignableTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmTaskCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmTaskQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmVariableValueDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.client.exception.ClientValidationException;
import com.epam.digital.data.platform.bpms.client.exception.TaskNotFoundException;
import feign.error.ErrorCodes;
import feign.error.ErrorHandling;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The interface extends {@link BaseFeignClient} and used to perform operations on camunda user
 * tasks
 */
@FeignClient(name = "camunda-task-client", url = "${bpms.url}/api")
public interface TaskRestClient extends BaseFeignClient {

  /**
   * Method for getting the number of camunda user tasks
   *
   * @param ddmTaskCountQueryDto object with search parameters
   * @return the number of camunda user tasks
   */
  @PostMapping("/task/count")
  @ErrorHandling
  DdmCountResultDto getTaskCountByParams(@RequestBody DdmTaskCountQueryDto ddmTaskCountQueryDto);

  /**
   * Method for completing camunda user task by id
   *
   * @param taskId          task identifier
   * @param completeTaskDto {@link DdmCompleteTaskDto} object
   * @return {@link DdmCompletedTaskDto} entity with root process instance info and task variables
   */
  @PostMapping("/extended/task/{id}/complete")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {422}, generate = ClientValidationException.class)
  })
  DdmCompletedTaskDto completeTaskById(@PathVariable("id") String taskId,
      @RequestBody DdmCompleteTaskDto completeTaskDto);

  @PostMapping("/task/{id}/claim")
  @ErrorHandling
  void claimTaskById(@PathVariable("id") String taskId,
      @RequestBody DdmClaimTaskQueryDto ddmClaimTaskQueryDto);

  /**
   * Returns a map containing task variables
   *
   * @param taskId task identifier
   * @return a map containing the {@link DdmCompleteTaskDto} entities
   */
  @GetMapping("/task/{taskId}/variables")
  @ErrorHandling
  Map<String, DdmVariableValueDto> getTaskVariables(@PathVariable("taskId") String taskId);

  /**
   * Method for getting list of camunda user tasks
   *
   * @param ddmTaskQueryDto object with search parameters
   * @return the list of {@link DdmTaskDto}
   */
  @PostMapping("/extended/task")
  @ErrorHandling
  List<DdmTaskDto> getTasksByParams(@RequestBody DdmTaskQueryDto ddmTaskQueryDto,
      @SpringQueryMap PaginationQueryDto paginationQueryDto);

  /**
   * Method for getting list of lightweight camunda user tasks
   *
   * @param ddmTaskQueryDto object with search parameters
   * @return the list of {@link DdmLightweightTaskDto}
   */
  @PostMapping("/extended/task/lightweight")
  @ErrorHandling
  List<DdmLightweightTaskDto> getLightweightTasksByParams(
      @RequestBody DdmTaskQueryDto ddmTaskQueryDto,
      @SpringQueryMap PaginationQueryDto paginationQueryDto);

  /**
   * Method for getting extended camunda user task
   *
   * @param id task instance id
   * @return {@link DdmSignableTaskDto} object
   */
  @GetMapping("/extended/task/{id}")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {404}, generate = TaskNotFoundException.class)
  })
  DdmSignableTaskDto getTaskById(@PathVariable("id") String id);
}
