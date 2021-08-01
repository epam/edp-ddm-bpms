package com.epam.digital.data.platform.bpms.service;

import com.epam.digital.data.platform.bpms.api.dto.StartFormQueryDto;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ResourceDefinition;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StartFormServiceImpl implements StartFormService {

  private final ProcessEngine processEngine;

  @Override
  public Map<String, String> getStartFormMap(StartFormQueryDto startFormQueryDto) {
    var formService = processEngine.getFormService();

    return processEngine.getRepositoryService().createProcessDefinitionQuery()
        .processDefinitionIdIn(startFormQueryDto.getProcessDefinitionIdIn().toArray(new String[0]))
        .list()
        .stream()
        .filter(ProcessDefinition::hasStartFormKey)
        .map(ResourceDefinition::getId)
        .distinct()
        .collect(Collectors.toMap(Function.identity(), formService::getStartFormKey));
  }
}
