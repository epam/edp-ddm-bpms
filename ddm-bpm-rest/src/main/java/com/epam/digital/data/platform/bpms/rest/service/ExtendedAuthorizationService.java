/*
 * Copyright 2023 EPAM Systems.
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

import com.epam.digital.data.platform.bpms.api.dto.DdmCountResultDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionAuthDto;
import com.epam.digital.data.platform.bpms.rest.service.repository.MybatisAuthorizationRepositoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * The service for managing process instances and process definitions authorizations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExtendedAuthorizationService {

  private final MybatisAuthorizationRepositoryService mybatisAuthorizationRepositoryService;

  /**
   * Create authorizations for process instances.
   *
   * @param groups list of group names for which authorizations will be created.
   * @return count of created authorizations.
   */
  public DdmCountResultDto createProcessInstanceAuthorizations(List<String> groups) {
    log.info("Starting saving process instance authorizations for groups {}", groups);
    var count = mybatisAuthorizationRepositoryService.createProcessInstanceAuthorizations(groups);
    log.info("Process instance authorizations created: {}", count);
    return new DdmCountResultDto(count);
  }

  /**
   * Create authorizations for process definitions.
   *
   * @param definitions list of {@link DdmProcessDefinitionAuthDto} for which authorizations will be
   *                    created.
   * @return count of created authorizations.
   */
  public DdmCountResultDto createProcessDefinitionAuthorizations(
      List<DdmProcessDefinitionAuthDto> definitions) {
    log.info("Starting saving process definition authorizations for definitions {}", definitions);
    var count = mybatisAuthorizationRepositoryService.createProcessDefinitionAuthorizations(
        definitions);
    log.info("Process definition authorizations created: {}", count);
    return new DdmCountResultDto(count);
  }

  /**
   * Delete authorizations for process instances and process definitions.
   *
   * @return count of removed authorizations.
   */
  public DdmCountResultDto deleteAuthorizations() {
    log.info("Starting deleting process instance/definition authorizations");
    var count = mybatisAuthorizationRepositoryService.deleteAuthorizations();
    log.info("Authorizations deleted: {}", count);
    return new DdmCountResultDto(count);
  }
}