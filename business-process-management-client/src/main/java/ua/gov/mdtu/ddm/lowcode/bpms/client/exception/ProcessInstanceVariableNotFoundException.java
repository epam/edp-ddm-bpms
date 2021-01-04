package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDto;

public class ProcessInstanceVariableNotFoundException extends NotFoundException {

  @FeignExceptionConstructor
  public ProcessInstanceVariableNotFoundException(ErrorDto errorDto) {
    super(errorDto);
  }
}
