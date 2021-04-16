package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import feign.error.FeignExceptionConstructor;

/**
 * The class represents an exception which will be thrown in case when a process instance was not
 * found.
 */
public class ProcessInstanceNotFoundException extends NotFoundException {

  @FeignExceptionConstructor
  public ProcessInstanceNotFoundException(SystemErrorDto errorDto) {
    super(errorDto);
  }
}