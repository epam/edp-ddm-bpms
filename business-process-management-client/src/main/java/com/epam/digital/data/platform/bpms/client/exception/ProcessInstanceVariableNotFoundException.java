package com.epam.digital.data.platform.bpms.client.exception;

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import feign.error.FeignExceptionConstructor;

/**
 * The class represents an exception which will be thrown in case when a process instance variable
 * was not found.
 */
public class ProcessInstanceVariableNotFoundException extends NotFoundException {

  @FeignExceptionConstructor
  public ProcessInstanceVariableNotFoundException(SystemErrorDto errorDto) {
    super(errorDto);
  }
}
