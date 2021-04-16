package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import feign.error.FeignExceptionConstructor;

/**
 * The class represents an exception which will be thrown in case when a process definition was not
 * found.
 */
public class ProcessDefinitionNotFoundException extends NotFoundException {

  @FeignExceptionConstructor
  public ProcessDefinitionNotFoundException(SystemErrorDto errorDto) {
    super(errorDto);
  }
}
