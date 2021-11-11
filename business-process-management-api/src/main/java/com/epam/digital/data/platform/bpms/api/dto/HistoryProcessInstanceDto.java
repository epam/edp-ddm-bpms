package com.epam.digital.data.platform.bpms.api.dto;

import com.epam.digital.data.platform.bpms.api.dto.enums.HistoryProcessInstanceStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryProcessInstanceDto {

  private String id;
  private String processDefinitionId;
  private String processDefinitionName;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private HistoryProcessInstanceStatus state;
  private String processCompletionResult;
  private String excerptId;
}
