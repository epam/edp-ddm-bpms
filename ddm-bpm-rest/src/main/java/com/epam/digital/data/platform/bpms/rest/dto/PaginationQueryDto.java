package com.epam.digital.data.platform.bpms.rest.dto;

import javax.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginationQueryDto {

  @QueryParam("firstResult")
  private Integer firstResult;

  @QueryParam("maxResults")
  private Integer maxResults;
}