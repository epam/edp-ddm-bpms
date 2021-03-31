package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.general.errorhandling.dto.SystemErrorDto;

/**
 * The class represents an exception which will be thrown in case when when a task was not found.
 */
public class TaskNotFoundException extends NotFoundException {

  @FeignExceptionConstructor
  public TaskNotFoundException(SystemErrorDto errorDto) {
    super(errorDto);
  }
}
