package com.epam.digital.data.platform.bpms.api.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representation of the user task.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTaskDto {

  private String id;
  private String name;
  private String assignee;
  private LocalDateTime created;
  private String description;
  private String processDefinitionName;
  private String processInstanceId;
  private String processDefinitionId;
  private String formKey;
  private boolean suspended;
}
