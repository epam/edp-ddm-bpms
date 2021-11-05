package com.epam.digital.data.platform.bpms.rest.controller;

import com.epam.digital.data.platform.bpms.api.dto.StartFormQueryDto;
import com.epam.digital.data.platform.bpms.engine.service.BatchFormService;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The class represents a controller that contains endpoint for getting process-definition
 * start-forms
 */
@Component
@RequiredArgsConstructor
@Path("/extended/start-form")
public class StartFormController {

  private final BatchFormService batchFormService;

  /**
   * POST method for getting start form keys. Returns a map, where key - processDefinitionId, value
   * - startFormKey.
   *
   * @param startFormQueryDto dto that contains query params for selecting form keys
   * @return a map containing the start form keys
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, String> getStartFormMap(StartFormQueryDto startFormQueryDto) {
    return batchFormService.getStartFormKeys(startFormQueryDto.getProcessDefinitionIdIn());
  }
}
