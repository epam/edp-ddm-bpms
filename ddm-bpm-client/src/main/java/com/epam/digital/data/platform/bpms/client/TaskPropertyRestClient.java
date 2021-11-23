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
