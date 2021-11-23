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

import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * The service with operations for managing and getting process definition data.
 */
public interface ProcessDefinitionImpersonatedService {

  /**
   * Get process definition names map.
   *
   * @param processDefinitionIds specified process definition ids.
   * @return map of process definition id and name.
   */
  Map<String, String> getProcessDefinitionsNames(List<String> processDefinitionIds);

  /**
   * Get process definition by id.
   *
   * @param id specified process definition id
   * @return process definition object
   */
  ProcessDefinition getProcessDefinition(String id);
}
