package ua.gov.mdtu.ddm.lowcode.bpms.client;

import feign.error.ErrorHandling;
import java.util.List;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.HistoryTaskQueryDto;

/**
 * The interface extends {@link BaseFeignClient} and used to perform operations on finished camunda
 * user tasks
 */
@FeignClient(name = "history-task-client", url = "${bpms.url}/api/history/task")
public interface HistoryTaskRestClient extends BaseFeignClient {

  /**
   * Method for getting list of finished camunda user tasks
   *
   * @param historyTaskQueryDto object with search parameters
   * @return the list of finished camunda user tasks
   */
  @GetMapping
  @ErrorHandling
  List<HistoricTaskInstanceEntity> getHistoryTasksByParams(
      @SpringQueryMap HistoryTaskQueryDto historyTaskQueryDto);
}
