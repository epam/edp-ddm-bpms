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

import lombok.Builder;
import lombok.Data;

/**
 * The class represents a data transfer object for building query to get process instances
 */
@Data
@Builder
@Deprecated(forRemoval = true)
public class DdmProcessInstanceQueryDto {

  private boolean rootProcessInstances;
  private String sortBy;
  private String sortOrder;

  /**
   * The class represents a list of constants that is used for process instance sorting.
   */
  public static final class SortByConstants {

    public static final String SORT_BY_INSTANCE_ID = "instanceId";
    public static final String SORT_BY_DEFINITION_ID = "definitionId";
    public static final String SORT_BY_START_TIME = "startTime";
    public static final String SORT_BY_DEFINITION_NAME = "definitionName";
    public static final String SORT_BY_DEFINITION_KEY = "definitionKey";
    public static final String SORT_BY_BUSINESS_KEY = "businessKey";
    public static final String SORT_BY_TENANT_ID = "tenantId";

    private SortByConstants() {
    }
  }
}
