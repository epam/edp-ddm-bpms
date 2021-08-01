package com.epam.digital.data.platform.bpms.client;

import com.epam.digital.data.platform.bpms.api.dto.StartFormQueryDto;
import feign.error.ErrorHandling;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The interface extends {@link BaseFeignClient} and used to perform operations on camunda process
 * definition start-forms
 */
@FeignClient(name = "camunda-process-definition-start-form-client", url = "${bpms.url}/api/extended/start-form")
public interface StartFormRestClient extends BaseFeignClient {

  /**
   * Method for getting start form keys. Returns a map, where key - processDefinitionId, value -
   * startFormKey.
   *
   * @param startFormQueryDto dto that contains query params for selecting form keys
   * @return a map containing the start form keys
   */
  @PostMapping
  @ErrorHandling
  Map<String, String> getStartFormKeyMap(@RequestBody StartFormQueryDto startFormQueryDto);
}
