package ua.gov.mdtu.ddm.lowcode.bpms.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CamundaSystemException extends RuntimeException {

  private String traceId;
  private String type;
  private String message;
  private String localizedMessage;

  public CamundaSystemException(String message) {
    super(message);
    this.message = message;
  }
}
