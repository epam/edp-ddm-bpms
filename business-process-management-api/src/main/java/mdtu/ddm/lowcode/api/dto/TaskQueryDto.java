package mdtu.ddm.lowcode.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskQueryDto {

  private String taskId;
  private String assignee;
}
