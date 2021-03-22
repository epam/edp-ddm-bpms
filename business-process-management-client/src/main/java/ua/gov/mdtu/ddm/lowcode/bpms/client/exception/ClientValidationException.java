package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.general.errorhandling.dto.ValidationErrorDto;
import ua.gov.mdtu.ddm.general.errorhandling.exception.ValidationException;

public class ClientValidationException extends ValidationException {

  @FeignExceptionConstructor
  public ClientValidationException(ValidationErrorDto validationErrorDto) {
    super(validationErrorDto);
  }
}
