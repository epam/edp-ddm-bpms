package com.epam.digital.data.platform.bpms.api.dto;

import lombok.Builder;
import lombok.Data;

/**
 * The class represents a data transfer object for building query to get history process instance.
 */
@Data
@Builder
public class HistoryProcessInstanceQueryDto {

  private boolean rootProcessInstances;
  private boolean unfinished;
  private boolean finished;
  private String sortBy;
  private String sortOrder;

  /**
   * The class represents a list of constants that is used for history process instance sorting.
   */
  public static final class SortByConstants {

    public static final String SORT_BY_INSTANCE_ID = "instanceId";
    public static final String SORT_BY_DEFINITION_ID = "definitionId";
    public static final String SORT_BY_DEFINITION_KEY = "definitionKey";
    public static final String SORT_BY_DEFINITION_NAME = "definitionName";
    public static final String SORT_BY_DEFINITION_VERSION = "definitionVersion";
    public static final String SORT_BY_BUSINESS_KEY = "businessKey";
    public static final String SORT_BY_START_TIME = "startTime";
    public static final String SORT_BY_END_TIME = "endTime";
    public static final String SORT_BY_DURATION = "duration";
    public static final String SORT_BY_TENANT_ID = "tenantId";

    private SortByConstants() {
    }
  }
}
