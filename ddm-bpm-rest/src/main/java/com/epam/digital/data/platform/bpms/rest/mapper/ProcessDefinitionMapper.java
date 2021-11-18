package com.epam.digital.data.platform.bpms.rest.mapper;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface ProcessDefinitionMapper {

  @Mapping(source = "startForm", target = "formKey")
  DdmProcessDefinitionDto toUserProcessDefinitionDto(ProcessDefinitionDto processDefinitionDto,
      String startForm);

  default List<DdmProcessDefinitionDto> toUserProcessDefinitionDtos(
      List<ProcessDefinitionDto> processDefinitionDtos, Map<String, String> startForms) {
    return processDefinitionDtos.stream()
        .map(processDefinitionDto -> {
          var startForm = startForms.get(processDefinitionDto.getId());
          return toUserProcessDefinitionDto(processDefinitionDto, startForm);
        })
        .collect(Collectors.toList());
  }
}
