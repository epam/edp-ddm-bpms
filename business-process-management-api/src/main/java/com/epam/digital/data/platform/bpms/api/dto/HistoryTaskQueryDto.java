package com.epam.digital.data.platform.bpms.api.dto;

import lombok.Builder;
import lombok.Data;

/**
 * The class represents a data transfer object for building query to get finished task.
 */
@Data
@Builder
public class HistoryTaskQueryDto {

  private String taskAssignee;
  private boolean finished;
}
