package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import org.springframework.http.HttpStatus;
import ua.gov.mdtu.ddm.general.errorhandling.dto.SystemErrorDto;
import ua.gov.mdtu.ddm.general.errorhandling.exception.RestSystemException;

/**
 * The class represents an exception which will be thrown in case when an error occurred during
 * communication with camunda.
 */
public class CamundaCommunicationException extends RestSystemException {

  @FeignExceptionConstructor
  public CamundaCommunicationException(SystemErrorDto errorDto) {
    super(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
