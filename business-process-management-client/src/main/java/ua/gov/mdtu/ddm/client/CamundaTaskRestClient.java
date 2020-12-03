package ua.gov.mdtu.ddm.client;

import java.util.List;
import mdtu.ddm.lowcode.api.dto.TaskQueryDto;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "camunda-task-client", url = "${bpms.url}/api/task")
public interface CamundaTaskRestClient {

  @GetMapping("/count")
  CountResultDto getTaskCountByParams(@SpringQueryMap TaskQueryDto taskQueryDto);

  @GetMapping
  List<TaskDto> getTasksByParams(@SpringQueryMap TaskQueryDto taskQueryDto);

  @GetMapping("/{id}")
  TaskDto getTaskById(@PathVariable("id") String taskId);
}
