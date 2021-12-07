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

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.dto.SystemVariablesDto;
import com.epam.digital.data.platform.bpms.rest.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.bpms.rest.service.repository.ProcessInstanceHistoricService;
import com.epam.digital.data.platform.bpms.rest.service.repository.VariableInstanceHistoricService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.springframework.stereotype.Service;

/**
 * The service for managing extended historical process instances.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoricProcessInstanceService {

  private final ProcessInstanceHistoricService processInstanceHistoricService;
  private final VariableInstanceHistoricService variableInstanceHistoricService;

  private final ProcessInstanceMapper processInstanceMapper;

  /**
   * Get historical process instances extended with completion result and excerpt id by query
   * params
   *
   * @param queryDto           object with search parameters.
   * @param paginationQueryDto object with pagination parameters.
   * @return list of {@link HistoryProcessInstanceDto}
   */
  public List<HistoryProcessInstanceDto> getHistoryProcessInstancesByParams(
      HistoricProcessInstanceQueryDto queryDto, PaginationQueryDto paginationQueryDto) {
    log.info("Getting historical process instances");

    var dtos = processInstanceHistoricService.getHistoryProcessInstanceDtos(queryDto,
        paginationQueryDto);
    log.trace("Found {} historic process instances", dtos.size());

    var variables = getSystemVariablesForProcessInstances(dtos);
    log.trace("Found system variables for {} historic process instances", variables.size());

    var result = processInstanceMapper.toHistoryProcessInstanceDtos(dtos, variables);
    log.info("Found {} historical process instances", dtos.size());

    return result;
  }

  /**
   * Get historical process instance extended with completion result and excerpt id by process
   * instance id
   *
   * @param id the process instanceId
   * @return {@link HistoryProcessInstanceDto}
   */
  public HistoryProcessInstanceDto getHistoryProcessInstanceDtoById(String id) {
    log.info("Getting historical process instance by id {}", id);

    var historicProcessInstance = processInstanceHistoricService.getHistoryProcessInstanceDto(id);
    log.trace("Historic process instance with id {} has been found", id);

    var variables = getSystemVariablesForProcessInstance(historicProcessInstance);
    log.trace("Found system variables for historic process instance with id {}", id);

    var result = processInstanceMapper.toHistoryProcessInstanceDto(historicProcessInstance,
        variables);
    log.trace("Historic process instance with id {} - {}", id, result);

    log.info("History process instance with id {} has been found", id);
    return result;
  }

  private SystemVariablesDto getSystemVariablesForProcessInstance(HistoricProcessInstanceDto dto) {
    var processInstanceId = dto.getId();

    var variables = variableInstanceHistoricService.getSystemVariablesForProcessInstanceIds(
        processInstanceId);

    return variables.get(processInstanceId);
  }

  private Map<String, SystemVariablesDto> getSystemVariablesForProcessInstances(
      List<HistoricProcessInstanceDto> historicProcessInstanceDtos) {
    var processInstanceIds = historicProcessInstanceDtos.stream()
        .map(HistoricProcessInstanceDto::getId)
        .toArray(String[]::new);

    return variableInstanceHistoricService.getSystemVariablesForProcessInstanceIds(
        processInstanceIds);
  }
}
