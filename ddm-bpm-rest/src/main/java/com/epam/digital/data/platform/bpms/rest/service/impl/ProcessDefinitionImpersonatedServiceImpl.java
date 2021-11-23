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

package com.epam.digital.data.platform.bpms.rest.service.impl;

import com.epam.digital.data.platform.bpms.rest.service.ProcessDefinitionImpersonatedService;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonation;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonationFactory;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ResourceDefinition;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessDefinitionImpersonatedServiceImpl implements
    ProcessDefinitionImpersonatedService {

  private final ProcessEngine processEngine;
  @Qualifier("camundaAdminImpersonationFactory")
  private final CamundaImpersonationFactory camundaImpersonationFactory;

  @Override
  public Map<String, String> getProcessDefinitionsNames(List<String> processDefinitionIds) {
    log.debug("Selecting process definitions for extracting names. Ids - {}", processDefinitionIds);
    var adminImpersonation = getAdminImpersonation();

    var processDefinitionQueryDto = new ProcessDefinitionQueryDto();
    processDefinitionQueryDto.setProcessDefinitionIdIn(processDefinitionIds);

    adminImpersonation.impersonate();
    try {
      var processDefinitionIdAndNameMap =
          toMap(processDefinitionQueryDto.toQuery(processEngine).list());
      log.info("Found {} process definitions - {}", processDefinitionIdAndNameMap.size(),
          processDefinitionIdAndNameMap);
      return toMap(processDefinitionQueryDto.toQuery(processEngine).list());
    } finally {
      adminImpersonation.revertToSelf();
    }
  }

  @Override
  public ProcessDefinition getProcessDefinition(String id) {
    var adminImpersonation = getAdminImpersonation();
    try {
      adminImpersonation.impersonate();
      return processEngine.getRepositoryService().getProcessDefinition(id);
    } finally {
      adminImpersonation.revertToSelf();
    }
  }

  private Map<String, String> toMap(List<ProcessDefinition> processDefinitions) {
    return processDefinitions.stream()
        .filter(pd -> Objects.nonNull(pd.getName()))
        .collect(Collectors.toMap(ProcessDefinition::getId, ResourceDefinition::getName));
  }

  private CamundaImpersonation getAdminImpersonation() {
    return camundaImpersonationFactory.getCamundaImpersonation()
        .orElseThrow(() -> new IllegalStateException(
            "Error occurred during accessing process definition info. "
                + "There is no user that authenticated in camunda"));
  }
}
