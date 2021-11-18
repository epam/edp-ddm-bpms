package com.epam.digital.data.platform.bpms.rest.service;

import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * The service with operations for managing and getting process definition data.
 */
public interface ProcessDefinitionImpersonatedService {

  /**
   * Get process definition names map.
   *
   * @param processDefinitionIds specified process definition ids.
   * @return map of process definition id and name.
   */
  Map<String, String> getProcessDefinitionsNames(List<String> processDefinitionIds);

  /**
   * Get process definition by id.
   *
   * @param id specified process definition id
   * @return process definition object
   */
  ProcessDefinition getProcessDefinition(String id);
}
