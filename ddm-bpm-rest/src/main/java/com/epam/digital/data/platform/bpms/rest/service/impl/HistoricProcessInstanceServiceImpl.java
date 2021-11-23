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

package com.epam.digital.data.platform.bpms.rest.service.impl;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.HistoryProcessInstanceStatus;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.bpms.rest.service.HistoricProcessInstanceService;
import com.epam.digital.data.platform.dataaccessor.sysvar.Constants;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.rest.history.HistoricProcessInstanceRestService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoricProcessInstanceServiceImpl implements HistoricProcessInstanceService {

  private final HistoricProcessInstanceRestService historicProcessInstanceRestService;
  private final ProcessInstanceMapper processInstanceMapper;

  private final HistoryService historyService;
  private final TaskRestService taskRestService;

  @Override
  public List<HistoryProcessInstanceDto> getHistoryProcessInstancesByParams(
      HistoricProcessInstanceQueryDto queryDto, PaginationQueryDto paginationQueryDto) {
    log.info("Getting historical process instances");

    var dtos = getHistoryProcessInstanceDtos(queryDto, paginationQueryDto);
    log.trace("Found {} historic process instances", dtos.size());

    var variables = getSystemVariablesForProcessInstances(dtos);
    log.trace("Found system variables for {} historic process instances", variables.size());

    var pendingProcessInstanceIds = getProcessInstanceIdsWithPendingTasks(dtos);
    log.trace("Found {} pending process instances", variables.size());

    var result = processInstanceMapper.toHistoryProcessInstanceDtos(dtos, variables,
        pendingProcessInstanceIds);
    log.info("Found {} historical process instances", dtos.size());

    return result;
  }

  @Override
  public HistoryProcessInstanceDto getHistoryProcessInstanceDtoById(String id) {
    log.info("Getting historical process instance by id {}", id);

    var historicProcessInstance = historicProcessInstanceRestService.getHistoricProcessInstance(id)
        .getHistoricProcessInstance();
    log.trace("Historic process instance with id {} has been found", id);

    var variables = getSystemVariablesForProcessInstance(historicProcessInstance);
    log.trace("Found {} system variables for historic process instance with id {}",
        variables.size(), id);

    var isProcessInstancePending = isProcessInstancePending(historicProcessInstance);
    log.trace("Pending process instance status is defined");

    var result = processInstanceMapper.toHistoryProcessInstanceDto(historicProcessInstance,
        variables, isProcessInstancePending);
    log.trace("Historic process instance with id {} - {}", id, result);

    log.info("History process instance with id {} has been found", id);
    return result;
  }

  private List<HistoricProcessInstanceDto> getHistoryProcessInstanceDtos(
      HistoricProcessInstanceQueryDto queryDto, PaginationQueryDto paginationQueryDto) {
    log.debug("Selecting history process instances...");
    var result = historicProcessInstanceRestService.queryHistoricProcessInstances(
        queryDto, paginationQueryDto.getFirstResult(), paginationQueryDto.getMaxResults());

    log.debug("Selected {} history process instances", result.size());
    return result;
  }

  private Map<String, String> getSystemVariablesForProcessInstance(HistoricProcessInstanceDto dto) {
    var processInstanceId = dto.getId();

    var variables = getSystemVariablesForProcessInstanceIds(processInstanceId);

    return variables.getOrDefault(processInstanceId, Map.of());
  }

  private Map<String, Map<String, String>> getSystemVariablesForProcessInstances(
      List<HistoricProcessInstanceDto> historicProcessInstanceDtos) {
    var processInstanceIds = historicProcessInstanceDtos.stream()
        .map(HistoricProcessInstanceDto::getId)
        .toArray(String[]::new);

    return getSystemVariablesForProcessInstanceIds(processInstanceIds);
  }

  private Map<String, Map<String, String>> getSystemVariablesForProcessInstanceIds(
      String... processInstanceIds) {
    log.debug("Selecting system variables for process instances {}",
        Arrays.toString(processInstanceIds));
    var result = historyService.createHistoricVariableInstanceQuery()
        .variableNameLike(Constants.SYS_VAR_PREFIX_LIKE)
        .processInstanceIdIn(processInstanceIds)
        .list().stream()
        .filter(variable -> Objects.nonNull(variable.getValue()))
        .collect(Collectors.groupingBy(HistoricVariableInstance::getProcessInstanceId,
            Collectors.toMap(HistoricVariableInstance::getName,
                instance -> (String) instance.getValue())));

    log.debug("Selected system variables for {} history process instances", result.size());
    return result;
  }

  private boolean isProcessInstancePending(HistoricProcessInstanceDto processInstance) {
    var id = processInstance.getId();

    var tasksByProcessInstance = getTasksByProcessInstanceIds(id);

    return !CollectionUtils.isEmpty(tasksByProcessInstance);
  }

  private Set<String> getProcessInstanceIdsWithPendingTasks(
      List<HistoricProcessInstanceDto> historicProcessInstanceDtos) {
    var activeProcessInstances = historicProcessInstanceDtos.stream()
        .filter(dto -> HistoryProcessInstanceStatus.ACTIVE.name().equals(dto.getState()))
        .map(HistoricProcessInstanceDto::getId)
        .toArray(String[]::new);

    if (ArrayUtils.isEmpty(activeProcessInstances)) {
      return Set.of();
    }

    var tasks = getTasksByProcessInstanceIds(activeProcessInstances);

    return tasks.stream()
        .map(TaskDto::getProcessInstanceId)
        .collect(Collectors.toSet());
  }

  private List<TaskDto> getTasksByProcessInstanceIds(String... activeProcessInstances) {
    log.debug("Selecting tasks for process instances - {}",
        Arrays.toString(activeProcessInstances));
    var taskQueryDto = new TaskQueryDto();
    taskQueryDto.setProcessInstanceIdIn(activeProcessInstances);
    var result = taskRestService.queryTasks(taskQueryDto, null, null);

    log.debug("Selected {} tasks", result.size());
    return result;
  }
}
