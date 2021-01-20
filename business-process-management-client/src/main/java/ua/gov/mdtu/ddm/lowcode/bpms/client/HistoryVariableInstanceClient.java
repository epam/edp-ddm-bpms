package ua.gov.mdtu.ddm.lowcode.bpms.client;

import java.util.List;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceDto;
import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.HistoryVariableInstanceQueryDto;

@FeignClient(name = "history-variable-instance-client",
    url = "${bpms.url}/api/history/variable-instance")
public interface HistoryVariableInstanceClient extends BaseFeignClient {

  @GetMapping
  @CollectionFormat(feign.CollectionFormat.CSV)
  List<HistoricVariableInstanceDto> getList(@SpringQueryMap HistoryVariableInstanceQueryDto dto);
}
