package com.epam.digital.data.platform.bpms.api.dto;

import com.epam.digital.data.platform.dso.api.dto.Subject;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representation of the user task.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignableUserTaskDto {

  private String id;
  private String taskDefinitionKey;
  private String name;
  private String assignee;
  private LocalDateTime created;
  private String description;
  private String processDefinitionName;
  private String processInstanceId;
  private String processDefinitionId;
  private String formKey;
  private boolean suspended;

  private boolean eSign;
  private Set<Subject> signatureValidationPack = Set.of();
  private Map<String, Object> formVariables = Map.of();
}
