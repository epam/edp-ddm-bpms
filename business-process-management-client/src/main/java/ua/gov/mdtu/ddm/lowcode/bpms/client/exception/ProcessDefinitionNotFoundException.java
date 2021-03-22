package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.general.errorhandling.dto.SystemErrorDto;

public class ProcessDefinitionNotFoundException extends NotFoundException {

  @FeignExceptionConstructor
  public ProcessDefinitionNotFoundException(SystemErrorDto errorDto) {
    super(errorDto);
  }
}
