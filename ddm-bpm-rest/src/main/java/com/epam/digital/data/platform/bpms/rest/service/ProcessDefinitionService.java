package com.epam.digital.data.platform.bpms.rest.service;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;

/**
 * The service with operations for managing and getting process definition data.
 */
public interface ProcessDefinitionService {

  /**
   * Get process definition key
   *
   * @param processDefinitionKey specified process definition key
   * @return map of process definition id and name.
   */
  DdmProcessDefinitionDto getUserProcessDefinitionDtoByKey(String processDefinitionKey);

  /**
   * Get process definition list by specified query
   *
   * @param queryDto specified process definition key
   * @return map of process definition id and name.
   */
  List<DdmProcessDefinitionDto> getUserProcessDefinitionDtos(ProcessDefinitionQueryDto queryDto);

}
