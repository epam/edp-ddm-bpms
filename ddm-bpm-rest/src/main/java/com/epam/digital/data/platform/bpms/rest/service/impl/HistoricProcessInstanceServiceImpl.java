package com.epam.digital.data.platform.bpms.rest.service.impl;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.bpms.rest.service.HistoricProcessInstanceService;
import com.epam.digital.data.platform.dataaccessor.sysvar.Constants;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.history.HistoricProcessInstanceRestService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoricProcessInstanceServiceImpl implements HistoricProcessInstanceService {

  private final HistoricProcessInstanceRestService historicProcessInstanceRestService;
  private final HistoryService historyService;
  private final ProcessInstanceMapper processInstanceMapper;

  @Override
  public List<HistoryProcessInstanceDto> getHistoryProcessInstancesByParams(
      HistoricProcessInstanceQueryDto queryDto, PaginationQueryDto paginationQueryDto) {
    log.info("Getting historical process instances");

    var dtos = getHistoryProcessInstanceDtos(queryDto, paginationQueryDto);
    log.trace("Found {} historic process instances", dtos.size());

    var variables = getSystemVariablesForProcessInstances(dtos);
    log.trace("Found system variables for {} historic process instances", variables.size());

    var result = processInstanceMapper.toHistoryProcessInstanceDtos(dtos, variables);
    log.info("Found {} historical process instances", dtos.size());

    return result;
  }

  @Override
  public HistoryProcessInstanceDto getHistoryProcessInstanceDtoById(String id) {
    var historicProcessInstance = historicProcessInstanceRestService.getHistoricProcessInstance(id)
        .getHistoricProcessInstance();

    var variables = getSystemVariablesForProcessInstance(historicProcessInstance);

    return processInstanceMapper.toHistoryProcessInstanceDto(historicProcessInstance, variables);
  }

  private List<HistoricProcessInstanceDto> getHistoryProcessInstanceDtos(
      HistoricProcessInstanceQueryDto queryDto, PaginationQueryDto paginationQueryDto) {
    return historicProcessInstanceRestService.queryHistoricProcessInstances(
        queryDto, paginationQueryDto.getFirstResult(), paginationQueryDto.getMaxResults());
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
    return historyService.createHistoricVariableInstanceQuery()
        .variableNameLike(Constants.SYS_VAR_PREFIX_LIKE)
        .processInstanceIdIn(processInstanceIds)
        .list().stream()
        .filter(variable -> Objects.nonNull(variable.getValue()))
        .collect(Collectors.groupingBy(HistoricVariableInstance::getProcessInstanceId,
            Collectors.toMap(HistoricVariableInstance::getName,
                instance -> (String) instance.getValue())));
  }
}
