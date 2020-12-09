package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDto;

public class CamundaCommunicationException extends CamundaServiceException {

  @FeignExceptionConstructor
  public CamundaCommunicationException(ErrorDto errorDto) {
    super(errorDto);
  }
}
