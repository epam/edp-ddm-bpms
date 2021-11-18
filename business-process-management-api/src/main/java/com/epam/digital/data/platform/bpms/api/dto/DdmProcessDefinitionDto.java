package com.epam.digital.data.platform.bpms.api.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class DdmProcessDefinitionDto {

  private final String id;
  private final String key;
  private final String name;
  private final boolean suspended;
  private final String formKey;
}
