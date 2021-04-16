package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import feign.error.FeignExceptionConstructor;

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
