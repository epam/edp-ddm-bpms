package ua.gov.mdtu.ddm.lowcode.bpms.client;

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "camunda-process-instance-client", url = "${bpms.url}/api/process-instance")
public interface ProcessInstanceRestClient {

  @GetMapping("/count")
  CountResultDto getProcessInstancesCount();
}
