package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.general.errorhandling.dto.SystemErrorDto;

public class ProcessInstanceNotFoundException extends NotFoundException {

  @FeignExceptionConstructor
  public ProcessInstanceNotFoundException(SystemErrorDto errorDto) {
    super(errorDto);
  }
}