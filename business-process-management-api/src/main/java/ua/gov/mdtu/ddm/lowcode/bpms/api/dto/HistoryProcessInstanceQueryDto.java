package ua.gov.mdtu.ddm.lowcode.bpms.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HistoryProcessInstanceQueryDto {

  private boolean unfinished;
  private String sortBy;
  private String sortOrder;

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
