package ua.gov.mdtu.ddm.lowcode.bpms.client;

import feign.error.ErrorCodes;
import feign.error.ErrorHandling;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.ProcessInstanceVariableNotFoundException;

@FeignClient(name = "camunda-process-instance-client", url = "${bpms.url}/api/process-instance")
public interface ProcessInstanceRestClient extends BaseFeignClient {

  @GetMapping("/count")
  @ErrorHandling
  CountResultDto getProcessInstancesCount();

  @GetMapping("/{processInstanceId}/variables/{variableName}")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {404}, generate = ProcessInstanceVariableNotFoundException.class)
  })
  VariableValueDto getProcessInstanceVariable(
      @PathVariable("processInstanceId") String processInstanceId,
      @PathVariable("variableName") String variableName);

  @PutMapping("/{processInstanceId}/variables/{variableName}")
  @ErrorHandling
  void putProcessInstanceVariable(
      @PathVariable("processInstanceId") String processInstanceId,
      @PathVariable("variableName") String variableName,
      @RequestBody VariableValueDto variableValueDto);
}
