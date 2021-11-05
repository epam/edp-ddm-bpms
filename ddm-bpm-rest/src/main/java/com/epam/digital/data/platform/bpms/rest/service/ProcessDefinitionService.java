package com.epam.digital.data.platform.bpms.rest.service;

import java.util.List;
import java.util.Map;

/**
 * The service with operations for managing and getting process definition data.
 */
public interface ProcessDefinitionService {

  /**
   * Get process definition names map.
   *
   * @param processDefinitionIds specified process definition ids.
   * @return map of process definition id and name.
   */
  Map<String, String> getProcessDefinitionsNames(List<String> processDefinitionIds);
}
