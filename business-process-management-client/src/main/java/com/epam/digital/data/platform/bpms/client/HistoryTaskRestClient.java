package com.epam.digital.data.platform.bpms.client;

import com.epam.digital.data.platform.bpms.api.dto.HistoryTaskCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryTaskQueryDto;
import feign.error.ErrorHandling;
import java.util.List;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

  /**
   * Method for getting the number of finished camunda user tasks
   *
   * @param historyTaskCountQueryDto object with search parameters
   * @return the number of finished camunda user tasks
   */
  @PostMapping("/count")
  @ErrorHandling
  CountResultDto getHistoryTaskCountByParams(
      @RequestBody HistoryTaskCountQueryDto historyTaskCountQueryDto);
}