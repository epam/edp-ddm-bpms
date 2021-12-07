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

import com.epam.digital.data.platform.bpms.api.dto.ProcessDefinitionDto;
import java.util.List;
import java.util.Map;
import org.mapstruct.Context;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface ProcessDefinitionMapper {

  @Mapping(source = "startForm", target = "formKey")
  ProcessDefinitionDto toDdmProcessDefinitionDto(
      org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto dto,
      String startForm);

  @Mapping(target = "formKey", expression = "java(startForms.get(dto.getId()))")
  @Named("toDdmProcessDefinitionDto")
  ProcessDefinitionDto toDdmProcessDefinitionDto(
      org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto dto,
      @Context Map<String, String> startForms);

  @IterableMapping(qualifiedByName = "toDdmProcessDefinitionDto")
  List<ProcessDefinitionDto> toDdmProcessDefinitionDtos(
      List<org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto> dtos,
      @Context Map<String, String> startForms);
}
