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

package com.epam.digital.data.platform.bpm.it.dto;

import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
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
