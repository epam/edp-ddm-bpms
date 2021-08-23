package com.epam.digital.data.platform.bpms.camunda.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CompleteActivityDto {

  private String processInstanceId;
  private String activityDefinitionId;
  private String completerUserName;
  private String completerAccessToken;
  private String expectedFormData;
}
