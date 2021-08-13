package com.epam.digital.data.platform.bpms.api.dto;

import lombok.Builder;
import lombok.Data;

/**
 * The class represents a data transfer object for building query to get count of finished tasks.
 */
@Data
@Builder
public class HistoryTaskCountQueryDto {

  private String taskAssignee;
  private Boolean finished;
}