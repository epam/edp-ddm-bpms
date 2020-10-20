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

package com.epam.digital.data.platform.bpm.history.base.mapper;

import com.epam.digital.data.platform.bphistory.model.HistoryProcess;
import com.epam.digital.data.platform.bphistory.model.HistoryTask;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessExcerptIdVariable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper that maps Camunda history events to custom dtos
 *
 * @see HistoryMapper#toHistoryProcess(HistoricProcessInstanceEventEntity)
 * @see HistoryMapper#toHistoryProcess(HistoricVariableUpdateEventEntity)
 * @see HistoryMapper#toHistoryTask(HistoricTaskInstanceEventEntity)
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface HistoryMapper {

  @Named("toLocalDateTime")
  default LocalDateTime toLocalDateTime(Date date) {
    if (Objects.isNull(date)) {
      return null;
    }
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
  }

  /**
   * Map camunda {@link HistoricProcessInstanceEventEntity} to {@link HistoryProcess}
   */
  @Mapping(target = "startTime", qualifiedByName = "toLocalDateTime")
  @Mapping(target = "endTime", qualifiedByName = "toLocalDateTime")
  HistoryProcess toHistoryProcess(HistoricProcessInstanceEventEntity entity);

  /**
   * Map camunda {@link HistoricVariableUpdateEventEntity} to {@link HistoryProcess}
   */
  @Mapping(target = "processDefinitionId", ignore = true)
  @Mapping(target = "processDefinitionKey", ignore = true)
  @Mapping(target = "processDefinitionName", ignore = true)
  @Mapping(target = "completionResult", source = "entity", qualifiedByName = "toCompletionResult")
  @Mapping(target = "excerptId", source = "entity", qualifiedByName = "toExcerptId")
  HistoryProcess toHistoryProcess(HistoricVariableUpdateEventEntity entity);

  @Named("toCompletionResult")
  default String toCompletionResult(HistoricVariableUpdateEventEntity entity) {
    if (ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT.equals(
        entity.getVariableName())) {
      return entity.getTextValue();
    }
    return null;
  }

  @Named("toExcerptId")
  default String toExcerptId(HistoricVariableUpdateEventEntity entity) {
    if (ProcessExcerptIdVariable.SYS_VAR_PROCESS_EXCERPT_ID.equals(entity.getVariableName())) {
      return entity.getTextValue();
    }
    return null;
  }

  /**
   * Map camunda {@link HistoricTaskInstanceEventEntity} to {@link HistoryTask}
   */
  @Mapping(target = "taskDefinitionName", source = "name")
  @Mapping(target = "startTime", qualifiedByName = "toLocalDateTime")
  @Mapping(target = "endTime", qualifiedByName = "toLocalDateTime")
  HistoryTask toHistoryTask(HistoricTaskInstanceEventEntity entity);
}
