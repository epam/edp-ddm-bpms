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

package com.epam.digital.data.platform.bpms.rest.service;

import com.epam.digital.data.platform.bpms.rest.cmd.GetStartFormKeysCmd;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.impl.ServiceImpl;
import org.springframework.stereotype.Component;

/**
 * The service for managing form keys.
 */
@Slf4j
@Component
public class BatchFormService extends ServiceImpl {

  /**
   * Get start form keys by provided process definition ids.
   *
   * @param processDefinitionIds specified process definition ids.
   * @return grouped start form keys by process definition ids.
   */
  public Map<String, String> getStartFormKeys(Set<String> processDefinitionIds) {
    log.info("Getting start form map for process definitions - {}", processDefinitionIds);
    var result = commandExecutor.execute(new GetStartFormKeysCmd(processDefinitionIds));
    log.info("Found {} start forms", result.size());
    return result;
  }
}
