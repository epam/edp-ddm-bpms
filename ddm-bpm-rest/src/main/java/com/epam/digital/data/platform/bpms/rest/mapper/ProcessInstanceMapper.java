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
import com.epam.digital.data.platform.bpms.rest.dto.SystemVariablesDto;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring",
    uses = LocalDateTimeMapper.class)
public interface ProcessInstanceMapper {

  @Mapping(target = "startTime", qualifiedByName = "toLocalDateTime")
  @Mapping(target = "endTime", qualifiedByName = "toLocalDateTime")
  @Mapping(target = "state", source = "dto.state")
  @Mapping(target = "processCompletionResult", source = "variables.processCompletionResult")
  @Mapping(target = "excerptId", source = "variables.excerptId")
  HistoryProcessInstanceDto toHistoryProcessInstanceDto(HistoricProcessInstanceDto dto,
      SystemVariablesDto variables);

  default List<HistoryProcessInstanceDto> toHistoryProcessInstanceDtos(
      List<HistoricProcessInstanceDto> dtos,
      Map<String, SystemVariablesDto> variables) {
    return dtos.stream()
        .map(dto -> {
          var processInstanceVariables = variables.get(dto.getId());

          return toHistoryProcessInstanceDto(dto, processInstanceVariables);
        })
        .collect(Collectors.toList());
  }
}
