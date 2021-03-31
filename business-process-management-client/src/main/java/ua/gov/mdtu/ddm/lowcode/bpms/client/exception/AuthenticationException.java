package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import org.springframework.http.HttpStatus;
import ua.gov.mdtu.ddm.general.errorhandling.dto.SystemErrorDto;
import ua.gov.mdtu.ddm.general.errorhandling.exception.RestSystemException;

/**
 * The class represents an exception for authentication error which will be thrown in case when
 * authentication was failed.
 */
public class AuthenticationException extends RestSystemException {

  @FeignExceptionConstructor
  public AuthenticationException(SystemErrorDto errorDto) {
    super(errorDto, HttpStatus.UNAUTHORIZED);
  }
}
