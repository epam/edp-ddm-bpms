/*
 * Copyright 2023 EPAM Systems.
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

import com.epam.digital.data.platform.bpms.api.dto.DdmCountResultDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionAuthDto;
import com.epam.digital.data.platform.bpms.rest.service.ExtendedAuthorizationService;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The controller that contains extended endpoints for managing authorizations.
 */
@Component
@RequiredArgsConstructor
@Path("/extended/authorizations")
public class ExtendedAuthorizationController {

  private final ExtendedAuthorizationService extendedAuthorizationService;

  /**
   * Create authorizations for process instances.
   *
   * @param groups list of group names for which authorizations will be created.
   * @return count of created authorizations.
   */
  @POST
  @Path("/process-instance/create")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public DdmCountResultDto createProcessInstanceAuthorizations(List<String> groups) {
    return extendedAuthorizationService.createProcessInstanceAuthorizations(groups);
  }

  /**
   * Create authorizations for process definitions.
   *
   * @param definitions list of {@link DdmProcessDefinitionAuthDto} for which authorizations will be
   *                    created.
   * @return count of created authorizations.
   */
  @POST
  @Path("/process-definition/create")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public DdmCountResultDto createProcessDefinitionAuthorizations(
      List<DdmProcessDefinitionAuthDto> definitions) {
    return extendedAuthorizationService.createProcessDefinitionAuthorizations(definitions);
  }

  /**
   * Delete authorizations for process instances and process definitions.
   *
   * @return count of removed authorizations.
   */
  @DELETE
  @Path("/delete")
  @Produces(MediaType.APPLICATION_JSON)
  public DdmCountResultDto deleteAuthorizations() {
    return extendedAuthorizationService.deleteAuthorizations();
  }
}