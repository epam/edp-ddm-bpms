package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import org.springframework.http.HttpStatus;
import ua.gov.mdtu.ddm.general.errorhandling.dto.SystemErrorDto;
import ua.gov.mdtu.ddm.general.errorhandling.exception.RestSystemException;

public class NotFoundException extends RestSystemException {

  @FeignExceptionConstructor
  public NotFoundException(SystemErrorDto errorDto) {
    super(errorDto, HttpStatus.NOT_FOUND);
  }
}
