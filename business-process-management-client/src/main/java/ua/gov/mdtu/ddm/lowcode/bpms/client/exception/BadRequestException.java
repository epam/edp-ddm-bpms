package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDto;

public class BadRequestException extends CamundaServiceException {

  @FeignExceptionConstructor
  public BadRequestException(ErrorDto errorDto) {
    super(errorDto);
  }
}
