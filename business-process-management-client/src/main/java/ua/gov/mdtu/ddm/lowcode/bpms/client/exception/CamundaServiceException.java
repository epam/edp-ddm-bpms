package ua.gov.mdtu.ddm.lowcode.bpms.client.exception;

import lombok.Getter;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDto;

@Getter
public class CamundaServiceException extends RuntimeException {

  private final String traceId;
  private final String type;
  private final String message;
  private final String localizedMessage;

  public CamundaServiceException(ErrorDto errorDto) {
    if (errorDto == null) {
      this.traceId = null;
      this.type = null;
      this.message = null;
      this.localizedMessage = null;
    } else {
      this.traceId = errorDto.getTraceId();
      this.type = errorDto.getType();
      this.message = errorDto.getMessage();
      this.localizedMessage = errorDto.getLocalizedMessage();
    }
  }
}
