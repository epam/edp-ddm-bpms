package com.epam.digital.data.platform.bpms.client;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.client.exception.ProcessInstanceNotFoundException;
import feign.error.ErrorCodes;
import feign.error.ErrorHandling;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The interface extends {@link BaseFeignClient} and used to perform operations on camunda historic
 * process instance
 */
@FeignClient(name = "camunda-history-process-instance-client", url = "${bpms.url}/api")
public interface HistoryProcessInstanceRestClient extends BaseFeignClient {

  /**
   * Method for getting list of finished camunda process instances
   *
   * @param historyProcessInstanceQueryDto object with search parameters
   * @param paginationQueryDto             object with pagination parameters
   * @return the list of finished camunda user tasks
   */
  @PostMapping("/extended/history/process-instance")
  @ErrorHandling
  List<HistoryProcessInstanceDto> getHistoryProcessInstanceDtosByParams(
      @RequestBody HistoryProcessInstanceQueryDto historyProcessInstanceQueryDto,
      @SpringQueryMap PaginationQueryDto paginationQueryDto);

  /**
   * Method for getting {@link HistoricProcessInstanceDto} entity by id
   *
   * @param id process instance identifier
   * @return a camunda historic process instance
   */
  @GetMapping("/extended/history/process-instance/{id}")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {404}, generate = ProcessInstanceNotFoundException.class)
  })
  HistoryProcessInstanceDto getProcessInstanceById(@PathVariable("id") String id);

  /**
   * Method for getting the number of camunda historic process instances
   *
   * @param query query map of possible parameters for request
   * @return the number of camunda historic process instances
   */
  @GetMapping("/history/process-instance/count")
  @ErrorHandling
  CountResultDto getProcessInstancesCount(@SpringQueryMap HistoryProcessInstanceCountQueryDto query);
}
