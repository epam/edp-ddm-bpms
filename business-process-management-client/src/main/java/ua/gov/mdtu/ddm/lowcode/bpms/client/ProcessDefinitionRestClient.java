package ua.gov.mdtu.ddm.lowcode.bpms.client;

import java.util.List;

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ProcessDefinitionQueryDto;

@FeignClient(name = "camunda-process-definition-client", url = "${bpms.url}/api/process-definition")
public interface ProcessDefinitionRestClient {

  @GetMapping("/count")
  CountResultDto getProcessDefinitionsCount(
      @SpringQueryMap ProcessDefinitionQueryDto requestDto);

  @GetMapping
  List<ProcessDefinitionDto> getProcessDefinitionsByParams(
      @SpringQueryMap ProcessDefinitionQueryDto requestDto);

  @GetMapping("/{id}")
  ProcessDefinitionDto getProcessDefinition(@PathVariable("id") String id);

  @PostMapping("/{id}/start")
  ProcessInstanceDto startProcessInstance(@PathVariable("id") String id,
                                          @RequestBody StartProcessInstanceDto startProcessInstanceDto);
}
