package com.epam.digital.data.platform.bpms.client;

import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.TaskQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import feign.error.ErrorHandling;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The interface extends {@link BaseFeignClient} and used to perform operations on extended user
 * tasks {@link UserTaskDto}.
 */
@FeignClient(name = "camunda-extended-task-client", url = "${bpms.url}/api/extended/task")
public interface ExtendedUserTaskRestClient extends BaseFeignClient {

  /**
   * Method for getting list of camunda user tasks
   *
   * @param taskQueryDto object with search parameters
   * @return the list of {@link UserTaskDto}
   */
  @PostMapping
  @ErrorHandling
  List<UserTaskDto> getTasksByParams(@RequestBody TaskQueryDto taskQueryDto, @SpringQueryMap
      PaginationQueryDto paginationQueryDto);
}
