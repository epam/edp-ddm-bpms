package com.epam.digital.data.platform.bpms.client.exception;

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.RestSystemException;
import feign.error.FeignExceptionConstructor;
import org.springframework.http.HttpStatus;

/**
 * The class represents an exception for 409-conflict error which will be thrown in case when
 * conflict in the current state of the resource was occurred.
 */
public class ConflictException extends RestSystemException {

  @FeignExceptionConstructor
  public ConflictException(SystemErrorDto systemErrorDto) {
    super(systemErrorDto, HttpStatus.CONFLICT);
  }
}
