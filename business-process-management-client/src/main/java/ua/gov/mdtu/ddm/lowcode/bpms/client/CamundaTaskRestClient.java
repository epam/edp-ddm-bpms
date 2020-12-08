package ua.gov.mdtu.ddm.lowcode.bpms.client;

import java.util.List;
import java.util.Map;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.TaskQueryDto;
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

@FeignClient(name = "camunda-task-client", url = "${bpms.url}/api/task")
public interface CamundaTaskRestClient {

  @GetMapping("/count")
  CountResultDto getTaskCountByParams(@SpringQueryMap TaskQueryDto taskQueryDto);

  @GetMapping
  List<TaskDto> getTasksByParams(@SpringQueryMap TaskQueryDto taskQueryDto);

  @GetMapping("/{id}")
  TaskDto getTaskById(@PathVariable("id") String taskId);

  @PostMapping("/{id}/complete")
  Map<String, VariableValueDto> completeTaskById(@PathVariable("id") String taskId,
      @RequestBody CompleteTaskDto completeTaskDto);
}
