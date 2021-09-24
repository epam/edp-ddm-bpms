package com.epam.digital.data.platform.bpms.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Data;

/**
 * The class represents a data transfer object for building query to get finished task.
 */
@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class HistoryTaskQueryDto {

  private String taskAssignee;
  private Boolean finished;
  private String sortBy;
  private String sortOrder;
  private Boolean unassigned;
}