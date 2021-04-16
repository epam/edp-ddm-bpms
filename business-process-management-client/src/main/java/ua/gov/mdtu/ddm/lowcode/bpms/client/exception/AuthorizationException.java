package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.RestSystemException;
import feign.error.FeignExceptionConstructor;
import org.springframework.http.HttpStatus;

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
