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

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.dto.ProcessInstanceExtendedQueryDto;
import com.epam.digital.data.platform.bpms.rest.service.ProcessInstanceService;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The controller that contains additional and extended endpoints for managing and getting
 * historical process instances.
 */
@Component
@RequiredArgsConstructor
@Path("/extended/process-instance")
public class ProcessInstanceController {

  private final ProcessInstanceService processInstanceService;

  /**
   * Get list of historical process-instances by provided query params
   *
   * @param queryDto           contains query params.
   * @param paginationQueryDto specified pagination.
   * @return list of {@link DdmProcessInstanceDto}
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public List<DdmProcessInstanceDto> getByParams(ProcessInstanceExtendedQueryDto queryDto,
      @BeanParam PaginationQueryDto paginationQueryDto) {
    return processInstanceService.getProcessInstancesByParams(queryDto, paginationQueryDto);
  }
}
