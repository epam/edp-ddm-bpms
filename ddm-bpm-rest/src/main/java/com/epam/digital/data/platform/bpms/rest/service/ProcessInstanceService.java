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

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.dto.ProcessInstanceExtendedQueryDto;
import com.epam.digital.data.platform.bpms.rest.dto.SystemVariablesDto;
import com.epam.digital.data.platform.bpms.rest.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.bpms.rest.service.repository.ProcessDefinitionRepositoryService;
import com.epam.digital.data.platform.bpms.rest.service.repository.ProcessInstanceRuntimeService;
import com.epam.digital.data.platform.bpms.rest.service.repository.TaskRuntimeService;
import com.epam.digital.data.platform.bpms.rest.service.repository.VariableInstanceRuntimeService;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonation;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.springframework.stereotype.Service;

/**
 * The service for managing process instances extended with process definition names, process
 * instance start time and pending status
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessInstanceService {

  private final ProcessInstanceRuntimeService processInstanceRuntimeService;
  private final VariableInstanceRuntimeService variableInstanceRuntimeService;
  private final ProcessDefinitionRepositoryService processDefinitionRepositoryService;
  private final TaskRuntimeService taskRuntimeService;

  @Resource(name = "camundaAdminImpersonation")
  private final CamundaImpersonation camundaAdminImpersonation;
  private final ProcessInstanceMapper processInstanceMapper;

  /**
   * Get process instances by query params.
   *
   * @param queryDto           object with search parameters.
   * @param paginationQueryDto object with pagination parameters.
   * @return list of {@link DdmProcessInstanceDto}
   */
  public List<DdmProcessInstanceDto> getProcessInstancesByParams(
      ProcessInstanceExtendedQueryDto queryDto, PaginationQueryDto paginationQueryDto) {
    log.info("Getting process instances");

    var dtos = processInstanceRuntimeService.getProcessInstanceDtos(queryDto, paginationQueryDto);
    log.trace("Found {} process instances", dtos.size());

    if (dtos.isEmpty()) {
      return List.of();
    }

    var systemVariablesDtos = getSystemVariablesDtos(dtos);
    log.trace("Found system variables for {} process instances", systemVariablesDtos.size());

    var processDefinitionNames = getProcessDefinitionNames(dtos);
    log.trace("Found {} process definition names", processDefinitionNames.size());

    var pendingProcessInstanceIds = getProcessInstanceIdsWithPendingTasks(dtos);
    log.trace("Found {} pending process instances", pendingProcessInstanceIds.size());

    var result = processInstanceMapper.toDdmProcessInstanceDtos(dtos, systemVariablesDtos,
        processDefinitionNames, pendingProcessInstanceIds);
    if (Objects.nonNull(queryDto.getCustomComparator())) {
      result.sort(queryDto.getCustomComparator());
    }
    log.info("Found {} process instances", dtos.size());
    return result;
  }

  private Map<String, SystemVariablesDto> getSystemVariablesDtos(
      List<ProcessInstanceDto> dtos) {
    var processInstanceIds = dtos.stream()
        .map(ProcessInstanceDto::getId)
        .toArray(String[]::new);

    return variableInstanceRuntimeService.getSystemVariablesForProcessInstanceIds(
        processInstanceIds);
  }

  private Map<String, String> getProcessDefinitionNames(
      List<ProcessInstanceDto> dtos) {
    var processDefinitionIds = dtos.stream()
        .map(ProcessInstanceDto::getDefinitionId)
        .distinct()
        .toArray(String[]::new);

    return camundaAdminImpersonation.execute(
        () -> processDefinitionRepositoryService.getProcessDefinitionsNames(processDefinitionIds));
  }

  private Set<String> getProcessInstanceIdsWithPendingTasks(
      List<ProcessInstanceDto> dtos) {
    var activeProcessInstances = dtos.stream()
        .map(ProcessInstanceDto::getId)
        .toArray(String[]::new);

    var tasks = getTasksByProcessInstanceIds(activeProcessInstances);

    return tasks.stream()
        .map(TaskDto::getProcessInstanceId)
        .collect(Collectors.toSet());
  }

  private List<TaskDto> getTasksByProcessInstanceIds(String... activeProcessInstances) {
    var taskQueryDto = new TaskQueryDto();
    taskQueryDto.setProcessInstanceIdIn(activeProcessInstances);
    return taskRuntimeService.getTasksByParams(taskQueryDto,
        PaginationQueryDto.builder().build());
  }
}
