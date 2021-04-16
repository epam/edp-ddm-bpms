package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import feign.error.FeignExceptionConstructor;

/**
 * The class represents an exception which will be thrown in case when when a task was not found.
 */
public class TaskNotFoundException extends NotFoundException {

  @FeignExceptionConstructor
  public TaskNotFoundException(SystemErrorDto errorDto) {
    super(errorDto);
  }
}
