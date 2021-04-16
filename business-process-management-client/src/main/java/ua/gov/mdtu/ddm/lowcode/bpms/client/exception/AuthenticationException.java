package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.RestSystemException;
import feign.error.FeignExceptionConstructor;
import org.springframework.http.HttpStatus;

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
