package com.epam.digital.data.platform.bpms.rest.service;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;

/**
 * The service for managing historical process instances.
 */
public interface HistoricProcessInstanceService {

  /**
   * Get historical process instances by query params.
   *
   * @param queryDto           object with search parameters.
   * @param paginationQueryDto object with pagination parameters.
   * @return list of {@link HistoryProcessInstanceDto}
   */
  List<HistoryProcessInstanceDto> getHistoryProcessInstancesByParams(
      HistoricProcessInstanceQueryDto queryDto,
      PaginationQueryDto paginationQueryDto);

  HistoryProcessInstanceDto getHistoryProcessInstanceDtoById(String id);
}
