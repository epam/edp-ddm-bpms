package com.epam.digital.data.platform.bpms.rest.service.repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ResourceDefinition;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.springframework.stereotype.Service;

/**
 * Repository service that is used for creating process definition queries to Camunda {@link
 * RepositoryService} and {@link ProcessEngine}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessDefinitionRepositoryService {

  private final RepositoryService repositoryService;
  private final ProcessEngine processEngine;

  /**
   * Get process definition name map by array of process definition ids.
   * <p>
   * Map structure:
   * <li>key - processDefinitionId</li>
   * <li>value - processDefinitionName</li>
   *
   * @param processDefinitionIds the process definition ids
   * @return the names map
   */
  public Map<String, String> getProcessDefinitionsNames(String... processDefinitionIds) {
    log.debug("Selecting process definitions for extracting names");
    var result = repositoryService.createProcessDefinitionQuery()
        .processDefinitionIdIn(processDefinitionIds)
        .list().stream()
        .filter(pd -> Objects.nonNull(pd.getName()))
        .collect(Collectors.toMap(ProcessDefinition::getId, ResourceDefinition::getName));

    log.debug("Found {} process definitions - {}", result.size(), result);
    return result;
  }

  /**
   * Get process definition with the latest version and no tenantId by process definition key
   *
   * @param processDefinitionKey the process definition key
   * @return optional value of {@link ProcessDefinitionDto process definition object}
   */
  public Optional<ProcessDefinitionDto> getProcessDefinitionDtoByKey(String processDefinitionKey) {
    log.debug("Selecting process definition by key {}", processDefinitionKey);

    var result = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .withoutTenantId()
        .latestVersion()
        .list().stream()
        .findFirst()
        .map(ProcessDefinitionDto::fromProcessDefinition);

    log.debug("Process instance {}" + (result.isPresent() ? "found" : "not found"));
    return result;
  }

  /**
   * Get process definition list by given parameters
   *
   * @param queryDto the parameter query dto
   * @return list of {@link ProcessDefinitionDto process definition object} that matches given
   * params
   */
  public List<ProcessDefinitionDto> getProcessDefinitionDtos(ProcessDefinitionQueryDto queryDto) {
    log.debug("Selecting camunda process definitions");

    var processDefinitions = queryDto.toQuery(processEngine).list().stream()
        .map(ProcessDefinitionDto::fromProcessDefinition)
        .collect(Collectors.toList());

    log.debug("Selected {} camunda process definitions", processDefinitions.size());
    return processDefinitions;
  }

  /**
   * Get process definition by id
   *
   * @param id the process definition id
   * @return {@link ProcessDefinitionDto process definition object}
   */
  public ProcessDefinition getProcessDefinitionById(String id) {
    return repositoryService.getProcessDefinition(id);
  }
}
