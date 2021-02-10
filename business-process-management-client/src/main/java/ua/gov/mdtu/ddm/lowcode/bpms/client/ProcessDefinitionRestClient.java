package ua.gov.mdtu.ddm.lowcode.bpms.client;

import feign.error.ErrorCodes;
import feign.error.ErrorHandling;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ProcessDefinitionQueryDto;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.ProcessDefinitionNotFoundException;

@FeignClient(name = "camunda-process-definition-client", url = "${bpms.url}/api/process-definition")
public interface ProcessDefinitionRestClient extends BaseFeignClient {

  @GetMapping("/count")
  @ErrorHandling
  @CollectionFormat(feign.CollectionFormat.CSV)
  CountResultDto getProcessDefinitionsCount(
      @SpringQueryMap ProcessDefinitionQueryDto requestDto);

  @GetMapping
  @ErrorHandling
  @CollectionFormat(feign.CollectionFormat.CSV)
  List<ProcessDefinitionDto> getProcessDefinitionsByParams(
      @SpringQueryMap ProcessDefinitionQueryDto requestDto);

  @GetMapping("/{id}")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {404}, generate = ProcessDefinitionNotFoundException.class)
  })
  ProcessDefinitionDto getProcessDefinition(@PathVariable("id") String id);

  @PostMapping("/{id}/start")
  @ErrorHandling
  ProcessInstanceDto startProcessInstance(@PathVariable("id") String id,
      @RequestBody StartProcessInstanceDto startProcessInstanceDto);
}
