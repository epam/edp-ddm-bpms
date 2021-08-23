package com.epam.digital.data.platform.bpms.camunda.dto;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AssertWaitingActivityDto {

  private String processDefinitionKey;
  private String processInstanceId;
  private String activityDefinitionId;
  private String formKey;
  private String assignee;
  @Builder.Default
  private List<String> candidateUsers = Collections.emptyList();
  @Builder.Default
  private List<String> candidateRoles = Collections.emptyList();
  @Builder.Default
  private Map<String, String> extensionElements = Collections.emptyMap();

  private FormDataDto expectedFormDataPrePopulation;
  @Builder.Default
  private Map<String, Object> expectedVariables = Collections.emptyMap();
}
