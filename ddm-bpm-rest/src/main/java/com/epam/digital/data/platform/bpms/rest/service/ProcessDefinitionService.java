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

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import com.epam.digital.data.platform.bpms.rest.mapper.ProcessDefinitionMapper;
import com.epam.digital.data.platform.bpms.rest.service.repository.ProcessDefinitionRepositoryService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.springframework.stereotype.Component;

/**
 * The service with operations for managing and getting process definition data extended with start
 * form keys.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessDefinitionService {

  private final ProcessDefinitionRepositoryService processDefinitionRepositoryService;
  private final BatchFormService batchFormService;

  private final ProcessDefinitionMapper processDefinitionMapper;

  /**
   * Get extended process definition by key
   *
   * @param processDefinitionKey specified process definition key
   * @return {@link DdmProcessDefinitionDto}
   */
  public DdmProcessDefinitionDto getDdmProcessDefinitionDtoByKey(String processDefinitionKey) {
    log.info("Starting selecting process definition by key {}", processDefinitionKey);

    var processDefinition = getProcessDefinitionByKey(processDefinitionKey);
    log.trace("Process definition by key {} found. Id - {}", processDefinitionKey,
        processDefinition.getId());

    var startForm = getFormKey(processDefinition.getId());
    log.trace("Found start form key for process definition by key {}. Id - {}",
        processDefinitionKey, processDefinition.getId());

    var result = processDefinitionMapper.toDdmProcessDefinitionDto(processDefinition, startForm);
    log.trace("Process definition by key {}. {}", processDefinitionKey, result);

    log.info("Process definition by key {} has been found.", processDefinitionKey);
    return result;
  }

  /**
   * Get extended process definition list by specified query
   *
   * @param queryDto specified process definition key
   * @return list of process definitions
   */
  public List<DdmProcessDefinitionDto> getDdmProcessDefinitionDtos(
      ProcessDefinitionQueryDto queryDto) {
    log.info("Starting selecting user process definitions");

    var processDefinitions = processDefinitionRepositoryService.getProcessDefinitionDtos(queryDto);
    log.trace("Found {} camunda process definitions", processDefinitions.size());

    var startForms = getStartForms(processDefinitions);
    log.trace("Found start form keys for {} process definitions", startForms.size());

    var result = processDefinitionMapper.toDdmProcessDefinitionDtos(processDefinitions,
        startForms);
    log.trace("Found user process definitions {}", result);

    log.info("{} user process definitions has been found.", result.size());
    return result;
  }

  private ProcessDefinitionDto getProcessDefinitionByKey(String processDefinitionKey) {
    log.debug("Selecting camunda process definition by key {}", processDefinitionKey);

    var result = processDefinitionRepositoryService.getProcessDefinitionDtoByKey(
        processDefinitionKey);

    if (result.isEmpty()) {
      String errorMessage = String.format(
          "No matching process definition with key: %s and no tenant-id", processDefinitionKey);
      throw new RestException(Status.NOT_FOUND, errorMessage);
    }

    log.debug("Selected process definition by key {}. Id - {}", processDefinitionKey,
        result.get().getId());
    return result.get();
  }

  private String getFormKey(String processDefinitionId) {
    log.debug("Selecting form key for process definition {}", processDefinitionId);

    var result = batchFormService.getStartFormKeys(Set.of(processDefinitionId))
        .get(processDefinitionId);

    log.debug("Selected form key for process definition {}", processDefinitionId);
    return result;
  }

  private Map<String, String> getStartForms(List<ProcessDefinitionDto> processDefinitions) {
    var processDefinitionIds = processDefinitions.stream()
        .map(ProcessDefinitionDto::getId)
        .collect(Collectors.toSet());
    log.debug("Selecting start forms for process definitions {}", processDefinitionIds);

    var startForms = batchFormService.getStartFormKeys(processDefinitionIds);

    log.debug("Selected start forms for {} process definitions", startForms.size());
    return startForms;
  }
}
