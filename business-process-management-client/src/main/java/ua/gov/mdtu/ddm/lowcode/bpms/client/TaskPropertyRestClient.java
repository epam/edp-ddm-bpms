package ua.gov.mdtu.ddm.lowcode.bpms.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "task-property-client", url = "${bpms.url}/api/extended/task")
public interface TaskPropertyRestClient extends BaseFeignClient {

  @GetMapping("/{id}/extension-element/property")
  Map<String, String> getTaskProperty(@PathVariable("id") String taskId);
}
