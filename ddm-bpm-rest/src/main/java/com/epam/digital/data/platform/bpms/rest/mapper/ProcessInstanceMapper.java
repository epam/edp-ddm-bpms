package com.epam.digital.data.platform.bpms.rest.mapper;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessExcerptIdVariable;
import java.util.List;
import java.util.Map;
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
  @Mapping(target = "processCompletionResult", source = "variables", qualifiedByName = "toProcessCompletionResult")
  @Mapping(target = "excerptId", source = "variables", qualifiedByName = "toExcerptId")
  HistoryProcessInstanceDto toHistoryProcessInstanceDto(
      HistoricProcessInstanceDto historicProcessInstanceDto, Map<String, String> variables);

  @Named("toProcessCompletionResult")
  default String toProcessCompletionResult(Map<String, String> variables) {
    return variables.get(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT);
  }

  @Named("toExcerptId")
  default String toExcerptId(Map<String, String> variables) {
    return variables.get(ProcessExcerptIdVariable.SYS_VAR_PROCESS_EXCERPT_ID);
  }

  default List<HistoryProcessInstanceDto> toHistoryProcessInstanceDtos(
      List<HistoricProcessInstanceDto> historicProcessInstanceDtos,
      Map<String, Map<String, String>> variables) {
    return historicProcessInstanceDtos.stream()
        .map(dto -> toHistoryProcessInstanceDto(dto, variables.getOrDefault(dto.getId(), Map.of())))
        .collect(Collectors.toList());
  }
}
