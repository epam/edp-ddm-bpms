package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.RestSystemException;
import feign.error.FeignExceptionConstructor;
import org.springframework.http.HttpStatus;

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
