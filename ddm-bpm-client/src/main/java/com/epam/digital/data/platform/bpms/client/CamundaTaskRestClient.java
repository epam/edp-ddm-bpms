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
import com.epam.digital.data.platform.bpms.api.dto.DdmTaskCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmTaskQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.client.exception.ClientValidationException;
import com.epam.digital.data.platform.bpms.client.exception.TaskNotFoundException;
import feign.error.ErrorCodes;
import feign.error.ErrorHandling;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
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
@FeignClient(name = "camunda-task-client", url = "${bpms.url}/api/task")
public interface CamundaTaskRestClient extends BaseFeignClient {

  /**
   * Method for getting the number of camunda user tasks
   *
   * @param ddmTaskCountQueryDto object with search parameters
   * @return the number of camunda user tasks
   */
  @PostMapping("/count")
  @ErrorHandling
  CountResultDto getTaskCountByParams(@RequestBody DdmTaskCountQueryDto ddmTaskCountQueryDto);

  /**
   * Method for getting list of camunda user tasks
   *
   * @param ddmTaskQueryDto object with search parameters
   * @return the list of camunda user tasks
   */
  @PostMapping
  @ErrorHandling
  List<TaskDto> getTasksByParams(@RequestBody DdmTaskQueryDto ddmTaskQueryDto, @SpringQueryMap
      PaginationQueryDto paginationQueryDto);

  /**
   * Method for getting camunda user task by task identifier
   *
   * @param taskId task identifier
   * @return the camunda user task
   */
  @GetMapping("/{id}")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {404}, generate = TaskNotFoundException.class)
  })
  TaskDto getTaskById(@PathVariable("id") String taskId);

  /**
   * Method for completing camunda user task by id
   *
   * @param taskId          task identifier
   * @param completeTaskDto {@link CompleteTaskDto} object
   * @return a map of {@link VariableValueDto} entities
   */
  @PostMapping("/{id}/complete")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {422}, generate = ClientValidationException.class)
  })
  Map<String, VariableValueDto> completeTaskById(@PathVariable("id") String taskId,
      @RequestBody CompleteTaskDto completeTaskDto);

  @PostMapping("/{id}/claim")
  @ErrorHandling
  void claimTaskById(@PathVariable("id") String taskId,
      @RequestBody DdmClaimTaskQueryDto ddmClaimTaskQueryDto);

  /**
   * Returns a map containing task variables
   *
   * @param taskId task identifier
   * @return a map containing the {@link VariableValueDto} entities
   */
  @GetMapping("/{taskId}/variables")
  @ErrorHandling
  Map<String, VariableValueDto> getTaskVariables(@PathVariable("taskId") String taskId);
}
