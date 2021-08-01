package com.epam.digital.data.platform.bpms.service;

import com.epam.digital.data.platform.bpms.api.dto.StartFormQueryDto;
import java.util.Map;

/**
 * The TaskPropertyService interface represents a service for getting extended task properties by
 * task identifier
 */
public interface StartFormService {

  /**
   * Returns a map, where key - process-definitionId, value - startFormKey.
   *
   * @param startFormQueryDto dto that contains query params
   * @return a map containing the start form keys
   */
  Map<String, String> getStartFormMap(StartFormQueryDto startFormQueryDto);
}
