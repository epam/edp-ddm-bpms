package com.epam.digital.data.platform.bpms.extension.delegate.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.camunda.spin.json.SpinJsonNode;

@Builder
@Getter
@ToString
public class RegistryConnectorResponse {

  private final int statusCode;
  private final transient SpinJsonNode responseBody;
}
