package com.epam.digital.data.platform.bpms.rest.service.repository;

import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceQueryDto;
import org.springframework.stereotype.Service;

/**
 * History service that is used for creating history task queries to Camunda {@link ProcessEngine}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskInstanceHistoricService {

  private final ProcessEngine engine;

  /**
   * Get historic task list by given parameters
   *
   * @param queryDto           the parameter query dto
   * @param paginationQueryDto query dto with pagination parameters
   * @return list of {@link HistoricTaskInstanceDto historic task object} that matches given params
   */
  public List<HistoricTaskInstanceDto> getHistoryUserTasksByParams(
      HistoricTaskInstanceQueryDto queryDto, PaginationQueryDto paginationQueryDto) {
    log.debug("Selecting history tasks...");

    var result = queryDto.toQuery(engine)
        .listPage(paginationQueryDto.getFirstResult(), paginationQueryDto.getMaxResults())
        .stream()
        .map(HistoricTaskInstanceDto::fromHistoricTaskInstance)
        .collect(Collectors.toList());

    log.debug("Selected {} history task instances", result.size());
    return result;
  }
}
