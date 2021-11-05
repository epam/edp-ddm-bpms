package com.epam.digital.data.platform.bpms.rest.controller;

import com.epam.digital.data.platform.bpms.api.dto.HistoryUserTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.service.HistoricTaskService;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceQueryDto;
import org.springframework.stereotype.Component;

/**
 * The controller that contains additional and extended endpoints for managing and getting
 * historical user tasks.
 */
@Component
@RequiredArgsConstructor
@Path("/extended/history/task")
public class HistoricTaskController {

  private final HistoricTaskService historicTaskService;

  /**
   * Get list of historical user tasks by provided query params.
   *
   * @param queryDto           contains query params.
   * @param paginationQueryDto specified pagination.
   * @return list of {@link UserTaskDto}
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public List<HistoryUserTaskDto> getByParams(HistoricTaskInstanceQueryDto queryDto,
      @BeanParam PaginationQueryDto paginationQueryDto) {
    return historicTaskService.getHistoryUserTasksByParams(queryDto, paginationQueryDto);
  }

}
