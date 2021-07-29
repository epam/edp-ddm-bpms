package com.epam.digital.data.platform.bpms.client;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.ProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.client.exception.ProcessInstanceVariableNotFoundException;
import feign.error.ErrorCodes;
import feign.error.ErrorHandling;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The interface extends {@link BaseFeignClient} and used to perform operations on camunda process
 * instance
 */
@FeignClient(name = "camunda-process-instance-client", url = "${bpms.url}/api/process-instance")
public interface ProcessInstanceRestClient extends BaseFeignClient {

  /**
   * Method for getting the number of camunda process instances
   *
   * @param query object with search parameters
   * @return the number of camunda process instances
   */
  @GetMapping("/count")
  @ErrorHandling
  CountResultDto getProcessInstancesCount(@SpringQueryMap ProcessInstanceCountQueryDto query);

  /**
   * Method for getting {@link VariableValueDto} entity
   *
   * @param processInstanceId process instance identifier
   * @param variableName      variable name
   * @return {@link VariableValueDto} entity
   */
  @GetMapping("/{processInstanceId}/variables/{variableName}")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {404}, generate = ProcessInstanceVariableNotFoundException.class)
  })
  VariableValueDto getProcessInstanceVariable(
      @PathVariable("processInstanceId") String processInstanceId,
      @PathVariable("variableName") String variableName);

  /**
   * Method for putting camunda process instance variable
   *
   * @param processInstanceId process instance identifier
   * @param variableName      variable name
   * @param variableValueDto  {@link VariableValueDto} entity
   */
  @PutMapping("/{processInstanceId}/variables/{variableName}")
  @ErrorHandling
  void putProcessInstanceVariable(
      @PathVariable("processInstanceId") String processInstanceId,
      @PathVariable("variableName") String variableName,
      @RequestBody VariableValueDto variableValueDto);
}
