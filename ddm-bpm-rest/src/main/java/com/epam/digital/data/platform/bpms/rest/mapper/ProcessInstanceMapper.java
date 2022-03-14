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

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.DdmProcessInstanceStatus;
import com.epam.digital.data.platform.bpms.rest.dto.SystemVariablesDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring",
    uses = LocalDateTimeMapper.class)
public interface ProcessInstanceMapper {

  @Mapping(target = "id", source = "dto.id")
  @Mapping(target = "processDefinitionId", source = "dto.definitionId")
  @Mapping(target = "processDefinitionName", source = "processDefinitionName")
  @Mapping(target = "state", source = "dto.suspended", qualifiedByName = "toProcessInstanceStatus")
  DdmProcessInstanceDto toDdmProcessInstanceDto(
      ProcessInstanceDto dto,
      SystemVariablesDto systemVariablesDto,
      String processDefinitionName,
      @Context boolean isPending);

  @Named("toProcessInstanceStatus")
  default DdmProcessInstanceStatus toProcessInstanceStatus(boolean isSuspended,
      @Context boolean isPending) {
    if (isSuspended) {
      return DdmProcessInstanceStatus.SUSPENDED;
    }
    return isPending ? DdmProcessInstanceStatus.PENDING : DdmProcessInstanceStatus.ACTIVE;
  }

  default List<DdmProcessInstanceDto> toDdmProcessInstanceDtos(
      List<ProcessInstanceDto> dtos,
      Map<String, SystemVariablesDto> systemVariablesDtos,
      Map<String, String> processDefinitionNames,
      Set<String> pendingProcessInstanceIds) {
    return dtos.stream()
        .map(dto -> {
          var variablesDto = systemVariablesDtos.get(dto.getId());
          var processDefinitionName = processDefinitionNames.get(dto.getDefinitionId());
          var isPending = pendingProcessInstanceIds.contains(dto.getId());

          return toDdmProcessInstanceDto(dto, variablesDto, processDefinitionName, isPending);
        })
        .collect(Collectors.toList());
  }
}
