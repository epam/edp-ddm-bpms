package com.epam.digital.data.platform.bpms.rest.service.repository;

import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.springframework.stereotype.Service;

/**
 * History service that is used for creating process instance queries to Camunda {@link
 * HistoryService} and {@link ProcessEngine}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessInstanceHistoricService {

  private final HistoryService historyService;
  private final ProcessEngine engine;

  /**
   * Get historic process instance list by given parameters
   *
   * @param queryDto           the parameter query dto
   * @param paginationQueryDto query dto with pagination parameters
   * @return list of {@link HistoricProcessInstanceDto historic process instance object} that
   * matches given params
   */
  public List<HistoricProcessInstanceDto> getHistoryProcessInstanceDtos(
      HistoricProcessInstanceQueryDto queryDto, PaginationQueryDto paginationQueryDto) {
    log.debug("Selecting history process instances...");

    var result = queryDto.toQuery(engine)
        .listPage(paginationQueryDto.getFirstResult(), paginationQueryDto.getMaxResults())
        .stream()
        .map(HistoricProcessInstanceDto::fromHistoricProcessInstance)
        .collect(Collectors.toList());

    log.debug("Selected {} history process instances", result.size());
    return result;
  }

  /**
   * Get historic process instance by id
   *
   * @param id the process instance id
   * @return {@link HistoricProcessInstanceDto historic process instance object}
   */
  public HistoricProcessInstanceDto getHistoryProcessInstanceDto(String id) {
    log.debug("Selecting history process instance by id {} ...", id);

    var instance = historyService.createHistoricProcessInstanceQuery().processInstanceId(id)
        .singleResult();
    log.debug("History process instance by id {} found", id);

    return HistoricProcessInstanceDto.fromHistoricProcessInstance(instance);
  }

  /**
   * Get historic process instance map by set of process instance ids
   * <p>
   * Map structure:
   * <li>key - processInstanceId</li>
   * <li>value - {@link HistoricProcessInstance historic process instance}</li>
   *
   * @param ids the process instance ids
   * @return the historic process instance map
   */
  public Map<String, HistoricProcessInstance> getHistoricProcessInstances(Set<String> ids) {
    log.debug("Selecting history process instances by ids {} ...", ids);

    var result = historyService.createHistoricProcessInstanceQuery()
        .processInstanceIds(ids)
        .list().stream()
        .collect(Collectors.toMap(HistoricProcessInstance::getId, Function.identity()));
    log.debug("{} history process instances found", result.size());
    return result;
  }
}
