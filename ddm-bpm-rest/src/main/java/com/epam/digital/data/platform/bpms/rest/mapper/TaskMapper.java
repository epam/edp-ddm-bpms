package com.epam.digital.data.platform.bpms.rest.mapper;

import com.epam.digital.data.platform.bpms.api.dto.HistoryUserTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.SignableUserTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring",
    uses = LocalDateTimeMapper.class)
public interface TaskMapper {

  @Mapping(target = "created", qualifiedByName = "toLocalDateTime")
  UserTaskDto toUserTaskDto(TaskDto taskDto);

  @Mapping(target = "created", qualifiedByName = "toLocalDateTime")
  SignableUserTaskDto toSignableUserTaskDto(TaskDto taskDto);

  @Mapping(target = "startTime", qualifiedByName = "toLocalDateTime")
  @Mapping(target = "endTime", qualifiedByName = "toLocalDateTime")
  HistoryUserTaskDto toHistoryUserTaskDto(HistoricTaskInstanceDto historicTaskInstanceDto);
}
