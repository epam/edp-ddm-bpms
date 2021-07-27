package com.epam.digital.data.platform.bpms.api.dto;

import lombok.Builder;
import lombok.Data;

/**
 * This class holds query parameters to get count of historic process instances
 */
@Data
@Builder
public class HistoryProcessInstanceCountQueryDto {

  private boolean rootProcessInstances;
  private boolean finished;

}
