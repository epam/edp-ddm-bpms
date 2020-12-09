package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDto;

public class TaskNotFoundException extends NotFoundException {

  @FeignExceptionConstructor
  public TaskNotFoundException(ErrorDto errorDto) {
    super(errorDto);
  }
}
