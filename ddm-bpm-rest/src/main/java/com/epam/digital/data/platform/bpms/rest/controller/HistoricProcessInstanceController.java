package com.epam.digital.data.platform.bpms.rest.controller;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.service.HistoricProcessInstanceService;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.springframework.stereotype.Component;

/**
 * The controller that contains additional and extended endpoints for managing and getting
 * historical process instances.
 */
@Component
@RequiredArgsConstructor
@Path("/extended/history/process-instance")
public class HistoricProcessInstanceController {

  private final HistoricProcessInstanceService historicProcessInstanceService;

  /**
   * Get list of historical process-instances by provided query params
   *
   * @param queryDto           contains query params.
   * @param paginationQueryDto specified pagination.
   * @return list of {@link HistoryProcessInstanceDto}
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public List<HistoryProcessInstanceDto> getByParams(HistoricProcessInstanceQueryDto queryDto,
      @BeanParam PaginationQueryDto paginationQueryDto) {
    return historicProcessInstanceService.getHistoryProcessInstancesByParams(queryDto,
        paginationQueryDto);
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public HistoryProcessInstanceDto getById(@PathParam("id") String id) {
    return historicProcessInstanceService.getHistoryProcessInstanceDtoById(id);
  }
}
