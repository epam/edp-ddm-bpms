package mdtu.ddm.lowcode.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessDefinitionQueryDto {

  private String name;
  private String sortBy;
  private String sortOrder;
}
