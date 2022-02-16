package com.epam.digital.data.platform.bpms.rest.service.repository;

import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
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

  private static final int MAX_NUMBER_OF_NESTED_SUB_PROCESSES = 2;

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
   * @param processInstance specified process instance id
   * @return {@link ProcessInstance process instance object}
   */
  public ProcessInstance getRootProcessInstance(ProcessInstance processInstance) {
    log.info("Getting root process instance for process instance {}", processInstance.getId());
    var nestedCount = 0;
    var currentProcessInstance = processInstance;
    while (nestedCount < MAX_NUMBER_OF_NESTED_SUB_PROCESSES &&
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

  /**
   * Get process instances that have the given process instance id as a sub process instance.
   *
   * @param processInstanceId specified process instance id
   * @return {@link ProcessInstance process instance object}
   */
  public Optional<ProcessInstance> getProcessInstanceBySubProcessInstanceId(
      String processInstanceId) {
    return runtimeService.createProcessInstanceQuery()
        .subProcessInstanceId(processInstanceId).list().stream().findFirst();
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

  /**
   * Get call activity process instances by root process instance id
   *
   * @param rootProcessInstanceId specified root process instance id
   * @return list of call activity {@link ProcessInstance process instance object} that are sub
   * processes for provided root process instance id
   */
  public List<ProcessInstance> getCallActivityProcessInstances(String rootProcessInstanceId) {
    return this.getCallActivityProcessInstances(rootProcessInstanceId, Lists.newArrayList());
  }

  private List<ProcessInstance> getCallActivityProcessInstances(String rootProcessInstanceId,
      List<ProcessInstance> processInstanceList) {
    if (processInstanceList.size() < MAX_NUMBER_OF_NESTED_SUB_PROCESSES) {
      var callActivityProcessInstance = getCallActivityProcessInstance(rootProcessInstanceId);
      if (callActivityProcessInstance.isEmpty()) {
        return processInstanceList;
      }
      processInstanceList.add(callActivityProcessInstance.get());

      return this.getCallActivityProcessInstances(callActivityProcessInstance.get().getId(),
          processInstanceList);
    }
    return processInstanceList;
  }

  private Optional<ProcessInstance> getCallActivityProcessInstance(String processInstanceId) {
    return runtimeService.createProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).list().stream()
        .reduce((instance1, instance2) -> {
          throw new IllegalStateException(
              "Found more than one call activity process instances by id");
        });
  }

}
