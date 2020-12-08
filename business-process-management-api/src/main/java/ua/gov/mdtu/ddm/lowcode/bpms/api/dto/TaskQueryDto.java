package ua.gov.mdtu.ddm.lowcode.bpms.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskQueryDto {

  private String taskId;
  private String assignee;
}
