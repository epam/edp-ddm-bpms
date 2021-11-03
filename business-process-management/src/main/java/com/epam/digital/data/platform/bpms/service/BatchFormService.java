package com.epam.digital.data.platform.bpms.service;

import java.util.Map;
import java.util.Set;

/**
 * The service for managing form keys.
 */
public interface BatchFormService {

  /**
   * Get start form keys by provided process definition ids.
   *
   * @param processDefinitionIds specified process definition ids.
   * @return grouped start form keys by process definition ids.
   */
  Map<String, String> getStartFormKeys(Set<String> processDefinitionIds);

}
