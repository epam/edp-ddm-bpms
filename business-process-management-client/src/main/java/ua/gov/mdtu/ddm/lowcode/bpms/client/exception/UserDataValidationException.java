package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import lombok.Getter;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.UserDataValidationErrorDto;

@Getter
public class UserDataValidationException extends RuntimeException {

  private UserDataValidationErrorDto error;

  @FeignExceptionConstructor
  public UserDataValidationException(UserDataValidationErrorDto error) {
    this.error = error;
  }
}
