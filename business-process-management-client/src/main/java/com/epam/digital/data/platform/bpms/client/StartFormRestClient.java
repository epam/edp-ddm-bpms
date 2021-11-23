/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
