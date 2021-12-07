package com.epam.digital.data.platform.bpms.rest.service.repository;

import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.springframework.stereotype.Service;

/**
 * Runtime service that is used for creating process instance queries to Camunda {@link
 * ProcessEngine}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessInstanceRuntimeService {

  private final ProcessEngine processEngine;

  /**
   * Get process instance list by given parameters
   *
   * @param queryDto           the parameter query dto
   * @param paginationQueryDto query dto with pagination parameters
   * @return list of {@link ProcessInstanceDto process instance object} that matches given params
   */
  public List<ProcessInstanceDto> getProcessInstanceDtos(ProcessInstanceQueryDto queryDto,
      PaginationQueryDto paginationQueryDto) {
    log.debug("Selecting process instances...");
    var result = queryDto.toQuery(processEngine)
        .listPage(paginationQueryDto.getFirstResult(), paginationQueryDto.getMaxResults())
        .stream().map(ProcessInstanceDto::fromProcessInstance)
        .collect(Collectors.toList());

    log.debug("Selected {} process instances", result.size());
    return result;
  }
}
