package com.epam.digital.data.platform.bpms.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * The class represents a data transfer object for building query to get task.
 */
@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class TaskQueryDto {

  private String taskId;
  private String assignee;
  private Boolean unassigned;
  private String processInstanceId;
  private List<TaskQueryDto> orQueries;
  private List<String> processInstanceIdIn;
  private List<SortingDto> sorting;
}
