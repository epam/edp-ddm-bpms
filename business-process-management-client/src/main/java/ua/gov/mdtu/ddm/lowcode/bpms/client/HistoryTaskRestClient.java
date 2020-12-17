package ua.gov.mdtu.ddm.lowcode.bpms.client;

import java.util.List;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.HistoryTaskQueryDto;

@FeignClient(name = "history-task-client", url = "${bpms.url}/api/history/task")
public interface HistoryTaskRestClient extends BaseFeignClient {

  @GetMapping
  List<TaskDto> getHistoryTasksByParams(@SpringQueryMap HistoryTaskQueryDto historyTaskQueryDto);
}
