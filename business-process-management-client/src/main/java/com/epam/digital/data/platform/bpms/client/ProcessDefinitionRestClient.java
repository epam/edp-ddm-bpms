package com.epam.digital.data.platform.bpms.client;

import com.epam.digital.data.platform.bpms.api.dto.ProcessDefinitionQueryDto;
import com.epam.digital.data.platform.bpms.client.exception.ProcessDefinitionNotFoundException;
import feign.error.ErrorCodes;
import feign.error.ErrorHandling;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.FormDto;
import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The interface extends {@link BaseFeignClient} and used to perform operations on camunda process
 * definition
 */
@FeignClient(name = "camunda-process-definition-client", url = "${bpms.url}/api/process-definition")
public interface ProcessDefinitionRestClient extends BaseFeignClient {

  /**
   * Method for getting the number of camunda process definitions
   *
   * @param requestDto object with search parameters
   * @return the number of process definitions
   */
  @GetMapping("/count")
  @ErrorHandling
  @CollectionFormat(feign.CollectionFormat.CSV)
  CountResultDto getProcessDefinitionsCount(
      @SpringQueryMap ProcessDefinitionQueryDto requestDto);

  /**
   * Method for getting list of camunda process definitions
   *
   * @param requestDto bject with search parameters
   * @return the list of camunda process definitions
   */
  @GetMapping
  @ErrorHandling
  @CollectionFormat(feign.CollectionFormat.CSV)
  List<ProcessDefinitionDto> getProcessDefinitionsByParams(
      @SpringQueryMap ProcessDefinitionQueryDto requestDto);

  /**
   * Method for getting camunda process definition by id
   *
   * @param id process definition identifier
   * @return a camunda process definition
   */
  @GetMapping("/{id}")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {404}, generate = ProcessDefinitionNotFoundException.class)
  })
  ProcessDefinitionDto getProcessDefinition(@PathVariable("id") String id);

  /**
   * Method for starting process instance by process definition id
   *
   * @param id                      process definition identifier
   * @param startProcessInstanceDto {@link StartProcessInstanceDto} entity
   * @return a started process instance
   */
  @PostMapping("/{id}/start")
  @ErrorHandling
  ProcessInstanceDto startProcessInstance(@PathVariable("id") String id,
      @RequestBody StartProcessInstanceDto startProcessInstanceDto);

  @GetMapping("/{id}/startForm")
  @ErrorHandling
  FormDto getStartForm(@PathVariable("id") String id);
}
