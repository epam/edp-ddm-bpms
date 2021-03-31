package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import org.springframework.http.HttpStatus;
import ua.gov.mdtu.ddm.general.errorhandling.dto.SystemErrorDto;
import ua.gov.mdtu.ddm.general.errorhandling.exception.RestSystemException;

/**
 * The class represents an exception for authorization error which will be thrown in case when
 * authorization was failed.
 */
public class AuthorizationException extends RestSystemException {

  @FeignExceptionConstructor
  public AuthorizationException(SystemErrorDto errorDto) {
    super(errorDto, HttpStatus.FORBIDDEN);
  }
}
