package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.RestSystemException;
import feign.error.FeignExceptionConstructor;
import org.springframework.http.HttpStatus;

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
