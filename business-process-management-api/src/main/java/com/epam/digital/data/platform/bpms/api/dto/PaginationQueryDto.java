package com.epam.digital.data.platform.bpms.api.dto;

import lombok.Builder;
import lombok.Data;

/**
 * The class represents a data transfer object for pagination parameters.
 */
@Data
@Builder
public class PaginationQueryDto {

  private Integer firstResult;
  private Integer maxResults;
}
