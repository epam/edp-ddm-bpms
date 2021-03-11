package ua.gov.mdtu.ddm.lowcode.bpms.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetailsDto {

  private List<ValidationErrorDto> errors;
}
