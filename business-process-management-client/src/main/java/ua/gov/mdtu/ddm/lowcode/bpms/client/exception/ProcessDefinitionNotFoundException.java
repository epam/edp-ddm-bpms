package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDto;

public class ProcessDefinitionNotFoundException extends NotFoundException {

  @FeignExceptionConstructor
  public ProcessDefinitionNotFoundException(ErrorDto errorDto) {
    super(errorDto);
  }
}
