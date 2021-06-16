package com.epam.digital.data.platform.bpms.delegate.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.camunda.spin.json.SpinJsonNode;

@Builder
@Getter
@ToString
public class EdrRegistryConnectorResponse {

  private final int statusCode;
  private final transient SpinJsonNode responseBody;
}
