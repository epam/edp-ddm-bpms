package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.general.errorhandling.dto.SystemErrorDto;

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
