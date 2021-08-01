package com.epam.digital.data.platform.bpms.api.dto;

import lombok.Builder;
import lombok.Data;

/**
 * This class holds query parameters to get count of process instances
 */
@Data
@Builder
public class ProcessInstanceCountQueryDto {
  
  private boolean rootProcessInstances;

}
