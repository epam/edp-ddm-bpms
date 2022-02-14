package com.epam.digital.data.platform.bpms.rest.service.repository;

import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

/**
 * Runtime service that is used for creating process instance queries to Camunda {@link
 * ProcessEngine}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessInstanceRuntimeService {

  private final RuntimeService runtimeService;
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
    return this.getProcessInstances(queryDto, paginationQueryDto)
        .stream().map(ProcessInstanceDto::fromProcessInstance)
        .collect(Collectors.toList());
  }

  public List<ProcessInstance> getProcessInstances(ProcessInstanceQueryDto queryDto,
      PaginationQueryDto paginationQueryDto) {
    log.debug("Selecting process instances...");
    var result = queryDto.toQuery(processEngine)
        .listPage(paginationQueryDto.getFirstResult(), paginationQueryDto.getMaxResults());
    log.debug("Selected {} process instances", result.size());
    return result;
  }

  /**
   * Get root process instance by provided process instance
   *
   * @param processInstance            specified process instance id
   * @param maxNumberOfNestedProcesses number of the nested processes
   * @return {@link ProcessInstance process instance object}
   */
  public ProcessInstance getRootProcessInstance(ProcessInstance processInstance,
      int maxNumberOfNestedProcesses) {
    log.info("Getting root process instance for process instance {}", processInstance.getId());
    assert maxNumberOfNestedProcesses > 0 : "Invalid number of maximum nested sub processes";
    var nestedCount = 0;
    var currentProcessInstance = processInstance;
    while (nestedCount < maxNumberOfNestedProcesses &&
        !processInstance.getId().equals(processInstance.getRootProcessInstanceId())) {
      currentProcessInstance =
          getProcessInstance(processInstance.getRootProcessInstanceId())
              .orElseThrow(() -> new IllegalStateException("Root process instance not found"));
      nestedCount++;
    }
    log.info("Got root process instance {}, for provided process instance {}",
        processInstance.getId(), currentProcessInstance.getId());
    return currentProcessInstance;
  }

  public Optional<ProcessInstance> getRootProcessInstance(String processInstanceId,
      int maxNumberOfNestedProcesses) {
    return getProcessInstance(processInstanceId).map(
        pi -> this.getRootProcessInstance(pi, maxNumberOfNestedProcesses));
  }

  public Optional<ProcessInstance> getProcessInstance(String processInstanceId) {
    log.debug("Selecting process instance by id {}...", processInstanceId);

    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .list().stream()
        .reduce((instance1, instance2) -> {
          throw new IllegalStateException("Found more than one process instances by id");
        });

    log.debug("Process instances by id {} {}", processInstanceId,
        processInstance.isEmpty() ? "not found" : "found");
    return processInstance;
  }
}
