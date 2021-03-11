package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import java.util.Objects;
import lombok.Getter;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDetailsDto;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.UserDataValidationErrorDto;

@Getter
public class UserDataValidationException extends RuntimeException {

  private String traceId;
  private String message;
  private String code;
  private ErrorDetailsDto details;

  @FeignExceptionConstructor
  public UserDataValidationException(UserDataValidationErrorDto error) {
    if (Objects.nonNull(error)) {
      this.traceId = error.getTraceId();
      this.message = error.getMessage();
      this.code = error.getCode();
      this.details = error.getDetails();
    }
  }
}
