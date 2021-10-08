package com.epam.digital.data.platform.bpms.service;

import com.epam.digital.data.platform.bpms.api.dto.HistoryUserTaskDto;
import com.epam.digital.data.platform.bpms.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.mapper.TaskMapper;
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
  private final ProcessDefinitionService processDefinitionService;
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
    var processDefinitionsIdAndNameMap = processDefinitionService.getProcessDefinitionsNames(
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
