package ua.gov.mdtu.ddm.lowcode.bpms.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HistoryVariableInstanceQueryDto {

  private String variableName;
  private String processInstanceId;
  private List<String> processInstanceIdIn;

}
