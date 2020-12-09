package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import lombok.Getter;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDto;

@Getter
public class CamundaServiceException extends RuntimeException {

  private final String type;
  private final String message;

  public CamundaServiceException(ErrorDto errorDto) {
    if (errorDto == null) {
      this.type = null;
      this.message = null;
    } else {
      this.type = errorDto.getType();
      this.message = errorDto.getMessage();
    }
  }
}
