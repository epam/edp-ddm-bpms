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

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.client.exception.ProcessInstanceVariableNotFoundException;
import feign.error.ErrorCodes;
import feign.error.ErrorHandling;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The interface extends {@link BaseFeignClient} and used to perform operations on camunda process
 * instance
 */
@FeignClient(name = "camunda-process-instance-client", url = "${bpms.url}/api")
public interface ProcessInstanceRestClient extends BaseFeignClient {

  /**
   * Method for getting the number of camunda process instances
   *
   * @param query object with search parameters
   * @return the number of camunda process instances
   */
  @Deprecated
  @GetMapping("/process-instance/count")
  @ErrorHandling
  CountResultDto getProcessInstancesCount(@SpringQueryMap DdmProcessInstanceCountQueryDto query);

  /**
   * Method for getting {@link VariableValueDto} entity
   *
   * @param processInstanceId process instance identifier
   * @param variableName      variable name
   * @return {@link VariableValueDto} entity
   */
  @GetMapping("/process-instance/{processInstanceId}/variables/{variableName}")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {404}, generate = ProcessInstanceVariableNotFoundException.class)
  })
  VariableValueDto getProcessInstanceVariable(
      @PathVariable("processInstanceId") String processInstanceId,
      @PathVariable("variableName") String variableName);

  /**
   * Method for putting camunda process instance variable
   *
   * @param processInstanceId process instance identifier
   * @param variableName      variable name
   * @param variableValueDto  {@link VariableValueDto} entity
   */
  @PutMapping("/process-instance/{processInstanceId}/variables/{variableName}")
  @ErrorHandling
  void putProcessInstanceVariable(
      @PathVariable("processInstanceId") String processInstanceId,
      @PathVariable("variableName") String variableName,
      @RequestBody VariableValueDto variableValueDto);

  @Deprecated
  @PostMapping("/extended/process-instance")
  @ErrorHandling
  List<DdmProcessInstanceDto> getProcessInstances(
      @RequestBody DdmProcessInstanceQueryDto ddmProcessInstanceQueryDto,
      @SpringQueryMap PaginationQueryDto paginationQueryDto);
}
