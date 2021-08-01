package com.epam.digital.data.platform.bpms.client;

import com.epam.digital.data.platform.bpms.api.dto.HistoryVariableInstanceQueryDto;
import feign.error.ErrorHandling;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The interface extends {@link BaseFeignClient} and used to perform operations on camunda history
 * variable instance
 */
@FeignClient(name = "history-variable-instance-client",
    url = "${bpms.url}/api/history/variable-instance")
public interface HistoryVariableInstanceClient extends BaseFeignClient {

  /**
   * Method for getting list of {@link HistoricVariableInstanceDto} entities
   *
   * @param dto object with search parameters
   * @return list of variable instances
   */
  @PostMapping
  @ErrorHandling
  List<HistoricVariableInstanceDto> getList(@RequestBody HistoryVariableInstanceQueryDto dto);
}
