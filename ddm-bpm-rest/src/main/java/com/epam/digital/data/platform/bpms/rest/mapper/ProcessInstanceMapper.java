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

package com.epam.digital.data.platform.bpms.rest.mapper;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.HistoryProcessInstanceStatus;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessExcerptIdVariable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring",
    uses = LocalDateTimeMapper.class)
public interface ProcessInstanceMapper {

  @Mapping(target = "startTime", qualifiedByName = "toLocalDateTime")
  @Mapping(target = "endTime", qualifiedByName = "toLocalDateTime")
  @Mapping(target = "state", expression = "java(toState(dto.getState(), isPending))")
  @Mapping(target = "processCompletionResult", source = "variables", qualifiedByName = "toProcessCompletionResult")
  @Mapping(target = "excerptId", source = "variables", qualifiedByName = "toExcerptId")
  HistoryProcessInstanceDto toHistoryProcessInstanceDto(
      HistoricProcessInstanceDto dto, Map<String, String> variables, boolean isPending);

  @Named("toProcessCompletionResult")
  default String toProcessCompletionResult(Map<String, String> variables) {
    return variables.get(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT);
  }

  @Named("toExcerptId")
  default String toExcerptId(Map<String, String> variables) {
    return variables.get(ProcessExcerptIdVariable.SYS_VAR_PROCESS_EXCERPT_ID);
  }

  @Named("toState")
  default HistoryProcessInstanceStatus toState(String state, boolean isPending) {
    if (isPending) {
      return HistoryProcessInstanceStatus.PENDING;
    }
    return Objects.nonNull(state) ? HistoryProcessInstanceStatus.valueOf(state) : null;
  }

  default List<HistoryProcessInstanceDto> toHistoryProcessInstanceDtos(
      List<HistoricProcessInstanceDto> dtos,
      Map<String, Map<String, String>> variables,
      Set<String> pendingProcessInstanceIds) {
    return dtos.stream()
        .map(dto -> {
          var processInstanceVariables = variables.get(dto.getId());
          var isPending = pendingProcessInstanceIds.contains(dto.getId());

          return toHistoryProcessInstanceDto(dto, processInstanceVariables, isPending);
        })
        .collect(Collectors.toList());
  }
}
