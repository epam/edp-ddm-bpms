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
import com.epam.digital.data.platform.bpms.rest.mapper.TaskMapper;
import com.epam.digital.data.platform.bpms.rest.service.repository.ProcessDefinitionRepositoryService;
import com.epam.digital.data.platform.bpms.rest.service.repository.TaskInstanceHistoricService;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonation;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceQueryDto;
import org.springframework.stereotype.Service;

/**
 * The service for managing historical user tasks extended with process definition name
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoricTaskService {

  private final TaskInstanceHistoricService taskInstanceHistoricService;
  private final ProcessDefinitionRepositoryService processDefinitionRepositoryService;

  @Resource(name = "camundaAdminImpersonation")
  private final CamundaImpersonation camundaAdminImpersonation;
  private final TaskMapper taskMapper;

  /**
   * Get historical user tasks extended with process definition name by query params
   *
   * @param queryDto           object with search parameters.
   * @param paginationQueryDto object with pagination parameters.
   * @return list of {@link HistoryUserTaskDto}
   */
  public List<HistoryUserTaskDto> getHistoryUserTasksByParams(HistoricTaskInstanceQueryDto queryDto,
      PaginationQueryDto paginationQueryDto) {
    log.info("Getting historical user tasks...");
    var historicTaskInstanceDtos = taskInstanceHistoricService.getHistoryUserTasksByParams(queryDto,
        paginationQueryDto);
    log.trace("Found {} historic tasks", historicTaskInstanceDtos.size());

    var processDefinitionNames = getProcessDefinitionNames(historicTaskInstanceDtos);
    log.trace("Found process definition names - {}", processDefinitionNames);

    var result = taskMapper.toHistoryUserTaskDtos(historicTaskInstanceDtos, processDefinitionNames);
    log.trace("Found historic task list - {}", result);

    log.info("Found {} historic user tasks", result.size());
    return result;
  }

  private Map<String, String> getProcessDefinitionNames(List<HistoricTaskInstanceDto> dtos) {
    var processDefinitionIds = dtos.stream()
        .map(HistoricTaskInstanceDto::getProcessDefinitionId)
        .toArray(String[]::new);
    return camundaAdminImpersonation.execute(
        () -> processDefinitionRepositoryService.getProcessDefinitionsNames(processDefinitionIds));
  }
}
