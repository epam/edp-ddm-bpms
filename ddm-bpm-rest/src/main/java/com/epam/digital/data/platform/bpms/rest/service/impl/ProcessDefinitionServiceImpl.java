package com.epam.digital.data.platform.bpms.rest.service.impl;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import com.epam.digital.data.platform.bpms.engine.service.BatchFormService;
import com.epam.digital.data.platform.bpms.rest.mapper.ProcessDefinitionMapper;
import com.epam.digital.data.platform.bpms.rest.service.ProcessDefinitionService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

  private final ProcessDefinitionMapper processDefinitionMapper;

  private final ProcessEngine processEngine;
  private final BatchFormService batchFormService;

  @Override
  public DdmProcessDefinitionDto getUserProcessDefinitionDtoByKey(String processDefinitionKey) {
    log.info("Starting selecting process definition by key {}", processDefinitionKey);

    var processDefinition = getProcessDefinitionDtoByKey(processDefinitionKey);
    log.trace("Process definition by key {} found. Id - {}", processDefinitionKey,
        processDefinition.getId());

    var startForm = getFormKey(processDefinition.getId());
    log.trace("Found start form key for process definition by key {}. Id - {}",
        processDefinitionKey, processDefinition.getId());

    var result = processDefinitionMapper.toUserProcessDefinitionDto(processDefinition, startForm);
    log.trace("Process definition by key {}. {}", processDefinitionKey, result);

    log.info("Process definition by key {} has been found.", processDefinitionKey);
    return result;
  }

  @Override
  public List<DdmProcessDefinitionDto> getUserProcessDefinitionDtos(
      ProcessDefinitionQueryDto queryDto) {
    log.info("Starting selecting user process definitions");

    var processDefinitions = getProcessDefinitionDtos(queryDto);
    log.trace("Found {} camunda process definitions", processDefinitions.size());

    var startForms = getStartForms(processDefinitions);
    log.trace("Found start form keys for {} process definitions", startForms.size());

    var result = processDefinitionMapper.toUserProcessDefinitionDtos(processDefinitions,
        startForms);
    log.trace("Found user process definitions {}", result);

    log.info("{} user process definitions has been found.", result.size());
    return result;
  }

  private ProcessDefinitionDto getProcessDefinitionDtoByKey(String processDefinitionKey) {
    log.debug("Selecting camunda process definition by key {}", processDefinitionKey);

    var result = processEngine.getRepositoryService()
        .createProcessDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .withoutTenantId()
        .latestVersion()
        .list().stream()
        .findFirst()
        .map(ProcessDefinitionDto::fromProcessDefinition);

    if (result.isEmpty()) {
      String errorMessage = String.format(
          "No matching process definition with key: %s and no tenant-id", processDefinitionKey);
      throw new RestException(Status.NOT_FOUND, errorMessage);
    }

    log.debug("Selected process definition by key {}. Id - {}", processDefinitionKey,
        result.get().getId());
    return result.get();
  }

  private String getFormKey(String processDefinitionId) {
    log.debug("Selecting form key for process definition {}", processDefinitionId);

    var result = batchFormService.getStartFormKeys(Set.of(processDefinitionId))
        .get(processDefinitionId);

    log.debug("Selected form key for process definition {}", processDefinitionId);
    return result;
  }

  private List<ProcessDefinitionDto> getProcessDefinitionDtos(ProcessDefinitionQueryDto queryDto) {
    log.debug("Selecting camunda process definitions");

    var processDefinitions = queryDto.toQuery(processEngine).list().stream()
        .map(ProcessDefinitionDto::fromProcessDefinition)
        .collect(Collectors.toList());

    log.debug("Selected {} camunda process definitions", processDefinitions.size());
    return processDefinitions;
  }

  private Map<String, String> getStartForms(List<ProcessDefinitionDto> processDefinitions) {
    var processDefinitionIds = processDefinitions.stream()
        .map(ProcessDefinitionDto::getId)
        .collect(Collectors.toSet());
    log.debug("Selecting start forms for process definitions {}", processDefinitionIds);

    var startForms = batchFormService.getStartFormKeys(processDefinitionIds);

    log.debug("Selected start forms for {} process definitions", startForms.size());
    return startForms;
  }
}
