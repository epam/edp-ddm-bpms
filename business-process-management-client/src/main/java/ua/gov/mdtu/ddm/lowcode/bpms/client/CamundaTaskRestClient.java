package ua.gov.mdtu.ddm.lowcode.bpms.client;

import feign.error.ErrorCodes;
import feign.error.ErrorHandling;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.TaskQueryDto;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.ClientValidationException;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.TaskNotFoundException;

/**
 * The interface extends {@link BaseFeignClient} and used to perform operations on camunda user
 * tasks
 */
@FeignClient(name = "camunda-task-client", url = "${bpms.url}/api/task")
public interface CamundaTaskRestClient extends BaseFeignClient {

  /**
   * Method for getting the number of camunda user tasks
   *
   * @param taskQueryDto object with search parameters
   * @return the number of camunda user tasks
   */
  @PostMapping("/count")
  @ErrorHandling
  CountResultDto getTaskCountByParams(@RequestBody TaskQueryDto taskQueryDto);

  /**
   * Method for getting list of camunda user tasks
   *
   * @param taskQueryDto object with search parameters
   * @return the list of camunda user tasks
   */
  @PostMapping
  @ErrorHandling
  List<TaskDto> getTasksByParams(@RequestBody TaskQueryDto taskQueryDto);

  /**
   * Method for getting camunda user task by task identifier
   *
   * @param taskId task identifier
   * @return the camunda user task
   */
  @GetMapping("/{id}")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {404}, generate = TaskNotFoundException.class)
  })
  TaskDto getTaskById(@PathVariable("id") String taskId);

  /**
   * Method for completing camunda user task by id
   *
   * @param taskId          task identifier
   * @param completeTaskDto {@link CompleteTaskDto} object
   * @return a map of {@link VariableValueDto} entities
   */
  @PostMapping("/{id}/complete")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {422}, generate = ClientValidationException.class)
  })
  Map<String, VariableValueDto> completeTaskById(@PathVariable("id") String taskId,
      @RequestBody CompleteTaskDto completeTaskDto);
}
