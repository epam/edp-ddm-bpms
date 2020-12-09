package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDto;

public class AuthorizationException extends CamundaServiceException {

  @FeignExceptionConstructor
  public AuthorizationException(ErrorDto errorDto) {
    super(errorDto);
  }
}
