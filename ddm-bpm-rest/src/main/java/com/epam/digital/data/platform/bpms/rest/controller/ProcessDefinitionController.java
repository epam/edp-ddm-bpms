package com.epam.digital.data.platform.bpms.rest.controller;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import com.epam.digital.data.platform.bpms.rest.service.ProcessDefinitionService;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.springframework.stereotype.Component;

/**
 * The controller that contains additional and extended endpoints for managing process definitions.
 */
@Component
@RequiredArgsConstructor
@Path("/extended/process-definition")
public class ProcessDefinitionController {

  private final ProcessDefinitionService processDefinitionService;

  /**
   * Get process definition by key
   *
   * @param key process definition key
   * @return {@link DdmProcessDefinitionDto process definition object}
   */
  @GET
  @Path("/key/{key}")
  @Produces(MediaType.APPLICATION_JSON)
  public DdmProcessDefinitionDto getByKey(@PathParam("key") String key) {
    return processDefinitionService.getUserProcessDefinitionDtoByKey(key);
  }

  /**
   * Get process definition by params
   *
   * @param params process definition params
   * @return list of {@link DdmProcessDefinitionDto process definition objects}
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public List<DdmProcessDefinitionDto> getByParams(ProcessDefinitionQueryDto params) {
    return processDefinitionService.getUserProcessDefinitionDtos(params);
  }
}
