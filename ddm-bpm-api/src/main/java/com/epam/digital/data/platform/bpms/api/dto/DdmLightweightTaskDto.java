package com.epam.digital.data.platform.bpms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DdmLightweightTaskDto {

  private String id;
  private String assignee;
}
