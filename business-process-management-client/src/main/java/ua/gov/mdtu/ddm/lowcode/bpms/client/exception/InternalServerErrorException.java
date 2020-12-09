package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDto;

public class InternalServerErrorException extends CamundaServiceException {

  @FeignExceptionConstructor
  public InternalServerErrorException(ErrorDto errorDto) {
    super(errorDto);
  }
}
