package com.epam.digital.data.platform.bpms.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * The class represents a data transfer object for building query to get count of tasks.
 */
@Data
@Builder
public class TaskCountQueryDto {

  private String assignee;
  private Boolean unassigned;
  private List<TaskCountQueryDto> orQueries;
}
