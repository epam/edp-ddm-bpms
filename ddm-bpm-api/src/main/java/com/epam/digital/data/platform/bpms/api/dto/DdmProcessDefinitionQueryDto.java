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

import java.util.List;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * The class represents a data transfer object for building query to get process definition.
 */
@Data
@Builder
public class DdmProcessDefinitionQueryDto {

  private boolean latestVersion;
  private String sortBy;
  private String sortOrder;
  private String processDefinitionId;
  private List<String> processDefinitionIdIn;
  @Default
  private boolean suspended = false;
  @Default
  private boolean active = false;

  /**
   * The class represents a list of constants that is used for process definitions sorting.
   */
  public static final class SortByConstants {

    public static final String SORT_BY_CATEGORY = "category";
    public static final String SORT_BY_KEY = "key";
    public static final String SORT_BY_ID = "id";
    public static final String SORT_BY_NAME = "name";
    public static final String SORT_BY_VERSION = "version";
    public static final String SORT_BY_DEPLOYMENT_ID = "deploymentId";
    public static final String SORT_BY_DEPLOY_TIME = "deployTime";
    public static final String SORT_BY_TENANT_ID = "tenantId";
    public static final String SORT_BY_VERSION_TAG = "versionTag";

    private SortByConstants() {
    }
  }
}
