package ua.gov.mdtu.ddm.lowcode.bpms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorDto {

  private String message;
  private String field;
  private String value;
}
