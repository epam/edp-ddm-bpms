package ua.gov.mdtu.ddm.lowcode.bpms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDataValidationErrorDto {

  private String type;
  private String message;
  private String traceId;
  private String localizedMessage;
  private ErrorDetailsDto details;
}
