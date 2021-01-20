package ua.gov.mdtu.ddm.lowcode.bpms.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.UserDataValidationErrorDto;

@Getter
@AllArgsConstructor
public class UserDataValidationException extends RuntimeException {

  private UserDataValidationErrorDto errorDto;
}
