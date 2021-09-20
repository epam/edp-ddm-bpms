package com.epam.digital.data.platform.bpms.service;

import com.epam.digital.data.platform.bpms.api.dto.StartFormQueryDto;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ResourceDefinition;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartFormServiceImpl implements StartFormService {

  private final ProcessEngine processEngine;

  @Override
  public Map<String, String> getStartFormMap(StartFormQueryDto startFormQueryDto) {
    log.info("Getting start form map for process definitions - {}",
        startFormQueryDto.getProcessDefinitionIdIn());
    var formService = processEngine.getFormService();

    var result = processEngine.getRepositoryService().createProcessDefinitionQuery()
        .processDefinitionIdIn(startFormQueryDto.getProcessDefinitionIdIn().toArray(new String[0]))
        .list()
        .stream()
        .filter(ProcessDefinition::hasStartFormKey)
        .map(ResourceDefinition::getId)
        .distinct()
        .collect(Collectors.toMap(Function.identity(), formService::getStartFormKey));
    log.info("Found {} start forms", result.size());
    return result;
  }
}
