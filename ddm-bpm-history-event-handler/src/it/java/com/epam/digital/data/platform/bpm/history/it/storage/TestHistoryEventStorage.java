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

package com.epam.digital.data.platform.bpm.history.it.storage;

import com.epam.digital.data.platform.bpm.history.base.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpm.history.base.dto.HistoryTaskDto;
import com.epam.digital.data.platform.bpm.history.base.publisher.ProcessHistoryEventPublisher;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;

@Getter
public class TestHistoryEventStorage {

  private final Map<String, HistoryProcessInstanceDto> processInstanceDtoMap = new HashMap<>();
  private final Map<String, HistoryTaskDto> historyTaskDtoMap = new HashMap<>();

  public void put(HistoryProcessInstanceDto dto) {
    processInstanceDtoMap.put(dto.getProcessInstanceId(), dto);
  }

  public void patch(HistoryProcessInstanceDto dto) {
    var existedDto = processInstanceDtoMap.get(dto.getProcessInstanceId());

    var newDto = new HistoryProcessInstanceDto(existedDto.getProcessInstanceId(),
        getOrElse(dto.getSuperProcessInstanceId(), existedDto.getSuperProcessInstanceId()),
        getOrElse(dto.getProcessDefinitionId(), existedDto.getProcessDefinitionId()),
        getOrElse(dto.getProcessDefinitionKey(), existedDto.getProcessDefinitionKey()),
        getOrElse(dto.getProcessDefinitionName(), existedDto.getProcessDefinitionName()),
        getOrElse(dto.getBusinessKey(), existedDto.getBusinessKey()),
        getOrElse(dto.getStartTime(), existedDto.getStartTime()),
        getOrElse(dto.getEndTime(), existedDto.getEndTime()),
        getOrElse(dto.getStartUserId(), existedDto.getStartUserId()),
        getOrElse(dto.getState(), existedDto.getState()),
        getOrElse(dto.getExcerptId(), existedDto.getExcerptId()),
        getOrElse(dto.getCompletionResult(), existedDto.getCompletionResult()));

    processInstanceDtoMap.put(newDto.getProcessInstanceId(), newDto);
  }

  public void put(HistoryTaskDto dto) {
    historyTaskDtoMap.put(dto.getActivityInstanceId(), dto);
  }

  public void patch(HistoryTaskDto dto) {
    var existedDto = historyTaskDtoMap.get(dto.getActivityInstanceId());

    var newDto = new HistoryTaskDto(existedDto.getActivityInstanceId(),
        getOrElse(dto.getTaskDefinitionKey(), existedDto.getTaskDefinitionKey()),
        getOrElse(dto.getTaskDefinitionName(), existedDto.getTaskDefinitionName()),
        getOrElse(dto.getProcessInstanceId(), existedDto.getProcessInstanceId()),
        getOrElse(dto.getProcessDefinitionId(), existedDto.getProcessDefinitionId()),
        getOrElse(dto.getProcessDefinitionKey(), existedDto.getProcessDefinitionKey()),
        getOrElse(dto.getProcessDefinitionName(), existedDto.getProcessDefinitionName()),
        getOrElse(dto.getRootProcessInstanceId(), existedDto.getRootProcessInstanceId()),
        getOrElse(dto.getStartTime(), existedDto.getStartTime()),
        getOrElse(dto.getEndTime(), existedDto.getEndTime()),
        getOrElse(dto.getAssignee(), existedDto.getAssignee()));

    historyTaskDtoMap.put(newDto.getActivityInstanceId(), newDto);
  }

  public HistoryProcessInstanceDto getHistoryProcessInstanceDto(String id) {
    return processInstanceDtoMap.get(id);
  }

  public HistoryTaskDto getHistoryTaskDto(String id) {
    return historyTaskDtoMap.get(id);
  }

  private <T> T getOrElse(T obj, T defaultObj) {
    return Objects.isNull(obj) ? defaultObj : obj;
  }
}
