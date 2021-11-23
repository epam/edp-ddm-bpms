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

import com.epam.digital.data.platform.bpms.api.dto.HistoryUserTaskDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.mapper.TaskMapper;
import com.epam.digital.data.platform.bpms.rest.service.HistoricTaskService;
import com.epam.digital.data.platform.bpms.rest.service.ProcessDefinitionImpersonatedService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceQueryDto;
import org.camunda.bpm.engine.rest.history.HistoricTaskInstanceRestService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoricTaskServiceImpl implements HistoricTaskService {

  private final HistoricTaskInstanceRestService historicTaskInstanceRestService;
  private final ProcessDefinitionImpersonatedService processDefinitionImpersonatedService;
  private final TaskMapper taskMapper;

  @Override
  public List<HistoryUserTaskDto> getHistoryUserTasksByParams(HistoricTaskInstanceQueryDto queryDto,
      PaginationQueryDto paginationQueryDto) {
    log.debug("Getting historical user tasks");
    var historicTaskInstanceDtos = historicTaskInstanceRestService.queryHistoricTaskInstances(
        queryDto, paginationQueryDto.getFirstResult(), paginationQueryDto.getMaxResults());
    log.trace("Found {} historic tasks", historicTaskInstanceDtos.size());

    var processDefinitionIds = historicTaskInstanceDtos.stream()
        .map(HistoricTaskInstanceDto::getProcessDefinitionId).collect(Collectors.toList());
    log.trace("Found {} process definition ids from task list. Result - {}",
        processDefinitionIds.size(), processDefinitionIds);
    var processDefinitionsIdAndNameMap = processDefinitionImpersonatedService.getProcessDefinitionsNames(
        processDefinitionIds);
    log.debug("Found process definition names - {}", processDefinitionsIdAndNameMap.values());

    var result = historicTaskInstanceDtos.stream()
        .map(historicTask -> toHistoryUserTaskDto(historicTask, processDefinitionsIdAndNameMap))
        .collect(Collectors.toList());
    log.info("Found {} historic user tasks", result.size());
    log.debug("Found historic task list - {}", result);
    return result;
  }

  private HistoryUserTaskDto toHistoryUserTaskDto(HistoricTaskInstanceDto historicTaskInstanceDto,
      Map<String, String> processDefinitionIdAndName) {
    var historyUserTask = taskMapper.toHistoryUserTaskDto(historicTaskInstanceDto);
    historyUserTask.setProcessDefinitionName(
        processDefinitionIdAndName.get(historyUserTask.getProcessDefinitionId()));
    return historyUserTask;
  }
}
