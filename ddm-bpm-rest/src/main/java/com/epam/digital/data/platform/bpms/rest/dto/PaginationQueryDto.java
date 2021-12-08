/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.bpms.rest.dto;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Query dto that contains pagination query parameters
 * <li>firstResult - default value {@code 0}</li>
 * <li>maxResults - default value {@code 2147483647} ({@code Integer.MAX_VALUE})</li>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginationQueryDto {

  @QueryParam("firstResult")
  @DefaultValue("0")
  @Builder.Default
  private Integer firstResult = 0;

  @QueryParam("maxResults")
  @DefaultValue("2147483647")
  @Builder.Default
  private Integer maxResults = Integer.MAX_VALUE;
}