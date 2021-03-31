package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import org.springframework.http.HttpStatus;
import ua.gov.mdtu.ddm.general.errorhandling.dto.SystemErrorDto;
import ua.gov.mdtu.ddm.general.errorhandling.exception.RestSystemException;

/**
 * The class represents an exception for internal server error which will be thrown in case when an
 * internal server error occurred.
 */
public class InternalServerErrorException extends RestSystemException {

  @FeignExceptionConstructor
  public InternalServerErrorException(SystemErrorDto errorDto) {
    super(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
