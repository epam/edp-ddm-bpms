package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import org.springframework.http.HttpStatus;
import ua.gov.mdtu.ddm.general.errorhandling.dto.SystemErrorDto;
import ua.gov.mdtu.ddm.general.errorhandling.exception.RestSystemException;

/**
 * The class represents an exception which will be thrown in case when the server cannot process the
 * client request.
 */
public class BadRequestException extends RestSystemException {

  @FeignExceptionConstructor
  public BadRequestException(SystemErrorDto errorDto) {
    super(errorDto, HttpStatus.BAD_REQUEST);
  }
}
