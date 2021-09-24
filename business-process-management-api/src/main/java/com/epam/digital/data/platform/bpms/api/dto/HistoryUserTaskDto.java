package com.epam.digital.data.platform.bpms.api.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representation of the historical user task.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryUserTaskDto {

  private String id;
  private String name;
  private String assignee;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String description;
  private String processDefinitionName;
  private String processInstanceId;
  private String processDefinitionId;
}
