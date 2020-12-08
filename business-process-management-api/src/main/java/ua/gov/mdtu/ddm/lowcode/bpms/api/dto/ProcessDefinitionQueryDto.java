package ua.gov.mdtu.ddm.lowcode.bpms.api.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessDefinitionQueryDto {
  private boolean latestVersion;
  private String sortBy;
  private String sortOrder;
  private String processDefinitionId;
  private List<String> processDefinitionIdIn;

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
