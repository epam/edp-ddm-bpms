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

  private String traceId;
  private String message;
  private String code;
  private ErrorDetailsDto details;
}
