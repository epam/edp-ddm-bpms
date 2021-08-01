package com.epam.digital.data.platform.bpms.client.exception;

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.RestSystemException;
import feign.error.FeignExceptionConstructor;
import org.springframework.http.HttpStatus;

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
