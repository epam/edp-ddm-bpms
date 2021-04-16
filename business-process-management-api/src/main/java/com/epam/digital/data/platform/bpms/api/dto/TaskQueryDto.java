package com.epam.digital.data.platform.bpms.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * The class represents a data transfer object for building query to get task.
 */
@Data
@Builder
public class TaskQueryDto {

  private String taskId;
  private String assignee;
  private String processInstanceId;
  private List<String> processInstanceIdIn;
}
