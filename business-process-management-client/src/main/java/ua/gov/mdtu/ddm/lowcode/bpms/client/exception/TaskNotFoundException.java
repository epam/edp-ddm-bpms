package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.general.errorhandling.dto.SystemErrorDto;

public class TaskNotFoundException extends NotFoundException {

  @FeignExceptionConstructor
  public TaskNotFoundException(SystemErrorDto errorDto) {
    super(errorDto);
  }
}
