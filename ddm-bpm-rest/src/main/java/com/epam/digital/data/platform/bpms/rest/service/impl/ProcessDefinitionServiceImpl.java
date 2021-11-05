package com.epam.digital.data.platform.bpms.rest.service.impl;

import com.epam.digital.data.platform.bpms.rest.service.ProcessDefinitionService;
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
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

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
