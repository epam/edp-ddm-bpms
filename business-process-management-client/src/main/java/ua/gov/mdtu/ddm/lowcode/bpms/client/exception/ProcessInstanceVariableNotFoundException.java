package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.general.errorhandling.dto.SystemErrorDto;

public class ProcessInstanceVariableNotFoundException extends NotFoundException {

  @FeignExceptionConstructor
  public ProcessInstanceVariableNotFoundException(SystemErrorDto errorDto) {
    super(errorDto);
  }
}
