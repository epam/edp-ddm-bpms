package com.epam.digital.data.platform.bpms.rest.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface LocalDateTimeMapper {

  @Named("toLocalDateTime")
  default LocalDateTime toLocalDateTime(Date date) {
    if (Objects.isNull(date)) {
      return null;
    }
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
  }
}
