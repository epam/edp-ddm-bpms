package ua.gov.mdtu.ddm.lowcode.bpms.client;

import feign.error.ErrorHandling;
import java.util.List;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.HistoryTaskQueryDto;

@FeignClient(name = "history-task-client", url = "${bpms.url}/api/history/task")
public interface HistoryTaskRestClient extends BaseFeignClient {

  @GetMapping
  @ErrorHandling
  List<HistoricTaskInstanceEntity> getHistoryTasksByParams(@SpringQueryMap HistoryTaskQueryDto historyTaskQueryDto);
}
