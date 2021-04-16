package com.epam.digital.data.platform.bpms.client;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.client.exception.ProcessInstanceNotFoundException;
import feign.error.ErrorCodes;
import feign.error.ErrorHandling;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * The interface extends {@link BaseFeignClient} and used to perform operations on camunda historic
 * process instance
 */
@FeignClient(name = "camunda-history-process-instance-client", url = "${bpms.url}/api/history/process-instance")
public interface ProcessInstanceHistoryRestClient extends BaseFeignClient {

  /**
   * Method for getting list of camunda historic process instances
   *
   * @param dto object with search parameters
   * @return the list of camunda historic process instances
   */
  @GetMapping
  @ErrorHandling
  List<HistoricProcessInstanceDto> getProcessInstances(
      @SpringQueryMap HistoryProcessInstanceQueryDto dto);

  /**
   * Method for getting {@link HistoricProcessInstanceDto} entity by id
   *
   * @param id process instance identifier
   * @return a camunda historic process instance
   */
  @GetMapping("/{id}")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {404}, generate = ProcessInstanceNotFoundException.class)
  })
  HistoricProcessInstanceDto getProcessInstanceById(@PathVariable("id") String id);
}
