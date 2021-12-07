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

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionQueryDto;
import com.epam.digital.data.platform.bpms.client.exception.ClientValidationException;
import com.epam.digital.data.platform.bpms.client.exception.ProcessDefinitionNotFoundException;
import feign.error.ErrorCodes;
import feign.error.ErrorHandling;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceWithVariablesDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.FormDto;
import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The interface extends {@link BaseFeignClient} and used to perform operations on camunda process
 * definition
 */
@FeignClient(name = "camunda-process-definition-client", url = "${bpms.url}/api")
public interface ProcessDefinitionRestClient extends BaseFeignClient {

  /**
   * Method for getting the number of camunda process definitions
   *
   * @param requestDto object with search parameters
   * @return the number of process definitions
   */
  @GetMapping("/process-definition/count")
  @ErrorHandling
  @CollectionFormat(feign.CollectionFormat.CSV)
  CountResultDto getProcessDefinitionsCount(
      @SpringQueryMap DdmProcessDefinitionQueryDto requestDto);

  /**
   * Method for getting list of camunda process definitions
   *
   * @param requestDto object with search parameters
   * @return the list of camunda process definitions
   */
  @PostMapping("/extended/process-definition")
  @ErrorHandling
  @CollectionFormat(feign.CollectionFormat.CSV)
  List<DdmProcessDefinitionDto> getProcessDefinitionsByParams(
      @RequestBody DdmProcessDefinitionQueryDto requestDto);

  /**
   * Method for getting camunda process definition by id
   *
   * @param id process definition identifier
   * @return a camunda process definition
   */
  @GetMapping("/process-definition/{id}")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {404}, generate = ProcessDefinitionNotFoundException.class)
  })
  ProcessDefinitionDto getProcessDefinition(@PathVariable("id") String id);

  /**
   * Method for getting camunda process definition by key
   *
   * @param key process definition key
   * @return a camunda process definition
   */
  @GetMapping("/extended/process-definition/key/{key}")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {404}, generate = ProcessDefinitionNotFoundException.class)
  })
  DdmProcessDefinitionDto getProcessDefinitionByKey(@PathVariable("key") String key);

  /**
   * Method for starting process instance by process definition id
   *
   * @param id                      process definition identifier
   * @param startProcessInstanceDto {@link StartProcessInstanceDto} entity
   * @return a started process instance
   */
  @PostMapping("/process-definition/{id}/start")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {422}, generate = ClientValidationException.class)
  })
  ProcessInstanceDto startProcessInstance(@PathVariable("id") String id,
      @RequestBody StartProcessInstanceDto startProcessInstanceDto);

  /**
   * Method for starting process instance by process definition key
   *
   * @param key                     process definition key
   * @param startProcessInstanceDto {@link StartProcessInstanceDto} entity
   * @return a started process instance with variables
   */
  @PostMapping("/process-definition/key/{key}/start")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {422}, generate = ClientValidationException.class)
  })
  ProcessInstanceWithVariablesDto startProcessInstanceByKey(
      @PathVariable("key") String key,
      @RequestBody StartProcessInstanceDto startProcessInstanceDto);

  /**
   * Method for getting start form by process definition id
   *
   * @param id process definition identifier
   * @return start form representation
   */
  @GetMapping("/process-definition/{id}/startForm")
  @ErrorHandling
  FormDto getStartForm(@PathVariable("id") String id);

  /**
   * Method for getting start form by process definition key
   *
   * @param key process definition key
   * @return start form representation
   */
  @GetMapping("/process-definition/key/{key}/startForm")
  @ErrorHandling
  FormDto getStartFormByKey(@PathVariable("key") String key);
}