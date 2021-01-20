package ua.gov.mdtu.ddm.lowcode.bpms.client;

import feign.error.ErrorCodes;
import feign.error.ErrorHandling;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.TaskQueryDto;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.TaskNotFoundException;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.UserDataValidationException;

@FeignClient(name = "camunda-task-client", url = "${bpms.url}/api/task")
public interface CamundaTaskRestClient extends BaseFeignClient {

  @GetMapping("/count")
  @CollectionFormat(feign.CollectionFormat.CSV)
  CountResultDto getTaskCountByParams(@SpringQueryMap TaskQueryDto taskQueryDto);

  @GetMapping
  @CollectionFormat(feign.CollectionFormat.CSV)
  List<TaskDto> getTasksByParams(@SpringQueryMap TaskQueryDto taskQueryDto);

  @GetMapping("/{id}")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {404}, generate = TaskNotFoundException.class)
  })
  TaskDto getTaskById(@PathVariable("id") String taskId);

  @PostMapping("/{id}/complete")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {422}, generate = UserDataValidationException.class)
  })
  Map<String, VariableValueDto> completeTaskById(@PathVariable("id") String taskId,
      @RequestBody CompleteTaskDto completeTaskDto);
}
