package ua.gov.mdtu.ddm.lowcode.bpms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDto {

  private String traceId;
  private String type;
  private String message;
  private String localizedMessage;
}
