package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import feign.error.FeignExceptionConstructor;
import org.springframework.http.HttpStatus;
import ua.gov.mdtu.ddm.general.errorhandling.dto.SystemErrorDto;
import ua.gov.mdtu.ddm.general.errorhandling.exception.RestSystemException;

public class InternalServerErrorException extends RestSystemException {

  @FeignExceptionConstructor
  public InternalServerErrorException(SystemErrorDto errorDto) {
    super(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
