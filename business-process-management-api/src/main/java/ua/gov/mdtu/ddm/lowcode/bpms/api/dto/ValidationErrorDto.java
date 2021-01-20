package ua.gov.mdtu.ddm.lowcode.bpms.api.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorDto {

  private String message;
  private Map<String, String> context;
}
