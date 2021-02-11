package ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.dto;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DataFactoryConnectorResponse {

  private final int statusCode;
  private final String responseBody;
  private final Map<String, List<String>> headers;
}
