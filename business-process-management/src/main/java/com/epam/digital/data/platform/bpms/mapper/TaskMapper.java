package com.epam.digital.data.platform.bpms.mapper;

import com.epam.digital.data.platform.bpms.api.dto.HistoryUserTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface TaskMapper {

  @Mapping(target = "created", qualifiedByName = "toLocalDateTime")
  UserTaskDto toUserTaskDto(TaskDto taskDto);

  @Mapping(target = "startTime", qualifiedByName = "toLocalDateTime")
  @Mapping(target = "endTime", qualifiedByName = "toLocalDateTime")
  HistoryUserTaskDto toHistoryUserTaskDto(HistoricTaskInstanceDto historicTaskInstanceDto);

  @Named("toLocalDateTime")
  default LocalDateTime toLocalDateTime(Date date) {
    if (Objects.isNull(date)) {
      return null;
    }
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
  }
}
