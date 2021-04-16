package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.RestSystemException;
import feign.error.FeignExceptionConstructor;
import org.springframework.http.HttpStatus;

/**
 * The class represents an exception which will be thrown in case when an object was not found
 */
public class NotFoundException extends RestSystemException {

  @FeignExceptionConstructor
  public NotFoundException(SystemErrorDto errorDto) {
    super(errorDto, HttpStatus.NOT_FOUND);
  }
}
