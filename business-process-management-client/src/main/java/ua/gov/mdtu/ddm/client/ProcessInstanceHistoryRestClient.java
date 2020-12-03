package ua.gov.mdtu.ddm.client;

import java.util.List;

import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

import mdtu.ddm.lowcode.api.dto.HistoryProcessInstanceQueryDto;

@FeignClient(name = "camunda-history-process-instance-client", url = "${bpms.url}/api/history/process-instance")
public interface ProcessInstanceHistoryRestClient {

  @GetMapping
  List<HistoricProcessInstanceDto> getProcessInstances(
      @SpringQueryMap HistoryProcessInstanceQueryDto dto);
}
