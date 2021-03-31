package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.general.errorhandling.dto.SystemErrorDto;

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
