package ua.gov.mdtu.ddm.lowcode.bpms.client;

import feign.error.ErrorHandling;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.HistoryVariableInstanceQueryDto;

@FeignClient(name = "history-variable-instance-client",
    url = "${bpms.url}/api/history/variable-instance")
public interface HistoryVariableInstanceClient extends BaseFeignClient {

  @PostMapping
  @ErrorHandling
  List<HistoricVariableInstanceDto> getList(@RequestBody HistoryVariableInstanceQueryDto dto);
}
