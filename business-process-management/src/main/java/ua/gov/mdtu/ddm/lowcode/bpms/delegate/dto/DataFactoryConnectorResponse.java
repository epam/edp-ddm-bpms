package ua.gov.mdtu.ddm.lowcode.bpms.delegate.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DataFactoryConnectorResponse implements Serializable {

  private final int statusCode;
  private final String responseBody;
  private final Map<String, List<String>> headers;
}
