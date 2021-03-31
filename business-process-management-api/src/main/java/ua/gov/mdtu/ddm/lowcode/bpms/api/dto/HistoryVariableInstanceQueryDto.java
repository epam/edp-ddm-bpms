package ua.gov.mdtu.ddm.lowcode.bpms.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * The class represents a data transfer object for building query to get history variable instance.
 */
@Data
@Builder
public class HistoryVariableInstanceQueryDto {

  private String variableName;
  private String processInstanceId;
  private List<String> processInstanceIdIn;

}
