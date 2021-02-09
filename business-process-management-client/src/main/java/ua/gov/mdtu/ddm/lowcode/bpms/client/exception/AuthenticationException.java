package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDto;

public class AuthenticationException extends CamundaServiceException {

  @FeignExceptionConstructor
  public AuthenticationException(ErrorDto errorDto) {
    super(errorDto);
  }
}
