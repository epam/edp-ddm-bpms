package ua.gov.mdtu.ddm.lowcode.bpms.delegate.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * The class represents a response that is used to map response from data factory.
 */
@Builder
@Getter
public class DataFactoryConnectorResponse implements Serializable {

  private final int statusCode;
  private final String responseBody;
  private final Map<String, List<String>> headers;
}
