package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.general.errorhandling.dto.ValidationErrorDto;
import ua.gov.mdtu.ddm.general.errorhandling.exception.ValidationException;

/**
 * The class represents an exception for client validation error which will be thrown in case when
 * client validation was failed.
 */
public class ClientValidationException extends ValidationException {

  @FeignExceptionConstructor
  public ClientValidationException(ValidationErrorDto validationErrorDto) {
    super(validationErrorDto);
  }
}
