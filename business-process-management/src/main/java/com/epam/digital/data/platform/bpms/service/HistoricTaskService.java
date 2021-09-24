package com.epam.digital.data.platform.bpms.service;

import com.epam.digital.data.platform.bpms.api.dto.HistoryUserTaskDto;
import com.epam.digital.data.platform.bpms.dto.PaginationQueryDto;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceQueryDto;

/**
 * The service for managing historical user tasks.
 */
public interface HistoricTaskService {

  /**
   * Get historical user tasks by query params.
   *
   * @param queryDto           object with search parameters.
   * @param paginationQueryDto object with pagination parameters.
   * @return list of {@link HistoryUserTaskDto}
   */
  List<HistoryUserTaskDto> getHistoryUserTasksByParams(HistoricTaskInstanceQueryDto queryDto,
      PaginationQueryDto paginationQueryDto);
}
