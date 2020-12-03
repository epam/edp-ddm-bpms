package mdtu.ddm.lowcode.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessDefinitionQueryDto {

  private String name;
  private String sortBy;
  private String sortOrder;
  private String processDefinitionId;
  private List<String> processDefinitionIdIn;
}
