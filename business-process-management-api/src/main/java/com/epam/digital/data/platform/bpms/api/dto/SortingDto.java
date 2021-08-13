package com.epam.digital.data.platform.bpms.api.dto;

import lombok.Builder;
import lombok.Data;

/**
 * The class represents a data transfer object for sorting parameters.
 */
@Data
@Builder
public class SortingDto {

  private String sortBy;
  private String sortOrder;
}
