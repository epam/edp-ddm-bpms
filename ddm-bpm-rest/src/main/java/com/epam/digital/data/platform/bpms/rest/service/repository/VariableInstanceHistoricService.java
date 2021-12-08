package com.epam.digital.data.platform.bpms.rest.service.repository;

import com.epam.digital.data.platform.bpms.rest.dto.SystemVariablesDto;
import com.epam.digital.data.platform.dataaccessor.sysvar.Constants;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.springframework.stereotype.Service;

/**
 * History service that is used for creating variable value queries to Camunda {@link
 * HistoryService}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VariableInstanceHistoricService {

  private final HistoryService historyService;

  /**
   * Get system variables value map by process instance id array
   * <p>
   * Map structure:
   * <li>key - process instance id</li>
   * <li>value - {@link SystemVariablesDto system variables object}</li>
   *
   * @param processInstanceIds the process instance id array
   * @return the system variables value map
   */
  public Map<String, SystemVariablesDto> getSystemVariablesForProcessInstanceIds(
      String... processInstanceIds) {
    log.debug("Selecting system variables for process instances {}",
        Arrays.toString(processInstanceIds));
    var result = historyService.createHistoricVariableInstanceQuery()
        .variableNameLike(Constants.SYS_VAR_PREFIX_LIKE)
        .processInstanceIdIn(processInstanceIds)
        .list().stream()
        .filter(variable -> Objects.nonNull(variable.getValue()))
        .collect(Collectors.groupingBy(HistoricVariableInstance::getProcessInstanceId,
            Collectors.collectingAndThen(Collectors.toMap(HistoricVariableInstance::getName,
                instance -> (String) instance.getValue()), SystemVariablesDto::new)));

    log.debug("Selected system variables for {} history process instances", result.size());
    return result;
  }
}
