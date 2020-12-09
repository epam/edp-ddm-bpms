package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDto;

public class NotFoundException extends CamundaServiceException {

  @FeignExceptionConstructor
  public NotFoundException(ErrorDto errorDto) {
    super(errorDto);
  }
}
