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
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.TaskQueryDto;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.TaskNotFoundException;

@FeignClient(name = "camunda-task-client", url = "${bpms.url}/api/task")
public interface CamundaTaskRestClient extends BaseFeignClient {

  @GetMapping("/count")
  CountResultDto getTaskCountByParams(@SpringQueryMap TaskQueryDto taskQueryDto);

  @GetMapping
  List<TaskDto> getTasksByParams(@SpringQueryMap TaskQueryDto taskQueryDto);

  @GetMapping("/{id}")
  @ErrorHandling(codeSpecific = {
      @ErrorCodes(codes = {404}, generate = TaskNotFoundException.class)
  })
  TaskDto getTaskById(@PathVariable("id") String taskId);

  @PostMapping("/{id}/complete")
  Map<String, VariableValueDto> completeTaskById(@PathVariable("id") String taskId,
      @RequestBody CompleteTaskDto completeTaskDto);
}
