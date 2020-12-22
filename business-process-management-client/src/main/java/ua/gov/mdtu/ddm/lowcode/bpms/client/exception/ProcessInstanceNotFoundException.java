package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDto;

public class ProcessInstanceNotFoundException extends NotFoundException {

  @FeignExceptionConstructor
  public ProcessInstanceNotFoundException(ErrorDto errorDto) {
    super(errorDto);
  }
}