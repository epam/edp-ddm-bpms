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

package com.epam.digital.data.platform.bpm.history.base.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Data transfer object that represents history Camunda process-instance entity
 */
@Getter
@ToString
@RequiredArgsConstructor
public class HistoryProcessInstanceDto {

  private final String processInstanceId;
  private final String superProcessInstanceId;
  private final String processDefinitionId;
  private final String processDefinitionKey;
  private final String processDefinitionName;
  private final String businessKey;
  private final LocalDateTime startTime;
  private final LocalDateTime endTime;
  private final String startUserId;
  private final String state;
  private final String excerptId;
  private final String completionResult;
}
