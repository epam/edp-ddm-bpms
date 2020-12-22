package ua.gov.mdtu.ddm.lowcode.bpms.client;

import feign.error.ErrorCodes;
import feign.error.ErrorHandling;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.HistoryProcessInstanceQueryDto;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.ProcessInstanceNotFoundException;

@FeignClient(name = "camunda-history-process-instance-client", url = "${bpms.url}/api/history/process-instance")
public interface ProcessInstanceHistoryRestClient extends BaseFeignClient {

  @GetMapping
  List<HistoricProcessInstanceDto> getProcessInstances(
      @SpringQueryMap HistoryProcessInstanceQueryDto dto);

  @GetMapping("/{id}")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {404}, generate = ProcessInstanceNotFoundException.class)
  })
  HistoricProcessInstanceDto getProcessInstanceById(@PathVariable("id") String id);
}
