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

package com.epam.digital.data.platform.bpms.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * The class represents a data transfer object for building query to get task.
 */
@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class DdmTaskQueryDto {

  private String taskId;
  private String assignee;
  private Boolean unassigned;
  private String processInstanceId;
  private String rootProcessInstanceId;
  private List<DdmTaskQueryDto> orQueries;
  private List<String> processInstanceIdIn;
  private List<SortingDto> sorting;
}
