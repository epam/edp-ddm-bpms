/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    return processDefinitionService.getDdmProcessDefinitionDtoByKey(key);
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
    return processDefinitionService.getDdmProcessDefinitionDtos(params);
  }
}
