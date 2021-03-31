package ua.gov.mdtu.ddm.lowcode.bpms.client;

import feign.error.ErrorHandling;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * The interface extends {@link BaseFeignClient} and used to perform operations on camunda user task
 * for getting extended task properties
 */
@FeignClient(name = "task-property-client", url = "${bpms.url}/api/extended/task")
public interface TaskPropertyRestClient extends BaseFeignClient {

  /**
   * Returns a map containing the extended properties of the task.
   *
   * @param taskId task identifier
   * @return a map containing the properties of the task
   */
  @GetMapping("/{id}/extension-element/property")
  @ErrorHandling
  Map<String, String> getTaskProperty(@PathVariable("id") String taskId);
}
