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

import com.epam.digital.data.platform.bpms.api.dto.DdmLightweightTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmSignableTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmTaskQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmVariableValueDto;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring",
    uses = LocalDateTimeMapper.class)
public interface TaskMapper {

  @Mapping(target = "created", qualifiedByName = "toLocalDateTime")
  @Mapping(target = "processDefinitionName", source = "processDefinitionName")
  @Mapping(target = "businessKey", source = "businessKey")
  @Named("toUserTaskDto")
  DdmTaskDto toDdmTaskDto(TaskDto dto, String processDefinitionName, String businessKey);

  default List<DdmTaskDto> toDdmTaskDtos(List<TaskDto> dtos,
      Map<String, String> processDefinitionNames,
      Map<String, String> processBusinessKeys) {
    return dtos.stream()
        .map(dto -> {
          var processDefinitionName = processDefinitionNames.get(dto.getId());
          var businessKey = processBusinessKeys.get(dto.getProcessInstanceId());

          return toDdmTaskDto(dto, processDefinitionName, businessKey);
        }).collect(Collectors.toList());
  }

  @Mapping(target = "created", qualifiedByName = "toLocalDateTime")
  DdmSignableTaskDto toSignableUserTaskDto(TaskDto taskDto);

  Map<String, DdmVariableValueDto> toDdmVariableValueDtoMap(Map<String, VariableValueDto> dtoMap);

  List<DdmLightweightTaskDto> toDdmLightweightTaskDtoList(List<TaskDto> taskDtos);

  TaskQueryDto toTaskQueryDto(DdmTaskQueryDto queryDto);
}
