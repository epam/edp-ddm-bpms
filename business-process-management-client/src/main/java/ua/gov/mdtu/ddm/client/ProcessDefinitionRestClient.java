package ua.gov.mdtu.ddm.client;

import java.util.List;
import mdtu.ddm.lowcode.api.dto.ProcessDefinitionQueryDto;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "camunda-process-definition-client", url = "${bpms.url}/api/process-definition")
public interface ProcessDefinitionRestClient {

  @GetMapping("/count")
  CountResultDto getProcessDefinitionsCount();

  @GetMapping
  List<ProcessDefinitionDto> getProcessDefinitionsByParams(
      @SpringQueryMap ProcessDefinitionQueryDto requestDto);
}
