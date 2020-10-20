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

import com.epam.digital.data.platform.bphistory.model.HistoryProcess;
import com.epam.digital.data.platform.bphistory.model.HistoryTask;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;

@Getter
public class TestHistoryEventStorage {

  private final Map<String, HistoryProcess> processInstanceDtoMap = new HashMap<>();
  private final Map<String, HistoryTask> historyTaskDtoMap = new HashMap<>();

  public void put(HistoryProcess dto) {
    processInstanceDtoMap.put(dto.getProcessInstanceId(), dto);
  }

  public void patch(HistoryProcess dto) {
    var existedDto = processInstanceDtoMap.get(dto.getProcessInstanceId());

    var newDto = new HistoryProcess();
    newDto.setProcessInstanceId(existedDto.getProcessInstanceId());
    newDto.setSuperProcessInstanceId(
        getOrElse(dto.getSuperProcessInstanceId(), existedDto.getSuperProcessInstanceId()));
    newDto.setProcessDefinitionId(
        getOrElse(dto.getProcessDefinitionId(), existedDto.getProcessDefinitionId()));
    newDto.setProcessDefinitionKey(
        getOrElse(dto.getProcessDefinitionKey(), existedDto.getProcessDefinitionKey()));
    newDto.setProcessDefinitionName(
        getOrElse(dto.getProcessDefinitionName(), existedDto.getProcessDefinitionName()));
    newDto.setBusinessKey(getOrElse(dto.getBusinessKey(), existedDto.getBusinessKey()));
    newDto.setStartTime(getOrElse(dto.getStartTime(), existedDto.getStartTime()));
    newDto.setEndTime(getOrElse(dto.getEndTime(), existedDto.getEndTime()));
    newDto.setStartUserId(getOrElse(dto.getStartUserId(), existedDto.getStartUserId()));
    newDto.setState(getOrElse(dto.getState(), existedDto.getState()));
    newDto.setExcerptId(getOrElse(dto.getExcerptId(), existedDto.getExcerptId()));
    newDto.setCompletionResult(
        getOrElse(dto.getCompletionResult(), existedDto.getCompletionResult()));

    processInstanceDtoMap.put(newDto.getProcessInstanceId(), newDto);
  }

  public void put(HistoryTask dto) {
    historyTaskDtoMap.put(dto.getActivityInstanceId(), dto);
  }

  public void patch(HistoryTask dto) {
    var existedDto = historyTaskDtoMap.get(dto.getActivityInstanceId());

    var newDto = new HistoryTask();
    newDto.setActivityInstanceId(existedDto.getActivityInstanceId());
    newDto.setTaskDefinitionKey(
        getOrElse(dto.getTaskDefinitionKey(), existedDto.getTaskDefinitionKey()));
    newDto.setTaskDefinitionName(
        getOrElse(dto.getTaskDefinitionName(), existedDto.getTaskDefinitionName()));
    newDto.setProcessInstanceId(
        getOrElse(dto.getProcessInstanceId(), existedDto.getProcessInstanceId()));
    newDto.setProcessDefinitionId(
        getOrElse(dto.getProcessDefinitionId(), existedDto.getProcessDefinitionId()));
    newDto.setProcessDefinitionKey(
        getOrElse(dto.getProcessDefinitionKey(), existedDto.getProcessDefinitionKey()));
    newDto.setProcessDefinitionName(
        getOrElse(dto.getProcessDefinitionName(), existedDto.getProcessDefinitionName()));
    newDto.setRootProcessInstanceId(
        getOrElse(dto.getRootProcessInstanceId(), existedDto.getRootProcessInstanceId()));
    newDto.setStartTime(getOrElse(dto.getStartTime(), existedDto.getStartTime()));
    newDto.setEndTime(getOrElse(dto.getEndTime(), existedDto.getEndTime()));
    newDto.setAssignee(getOrElse(dto.getAssignee(), existedDto.getAssignee()));

    historyTaskDtoMap.put(newDto.getActivityInstanceId(), newDto);
  }

  public HistoryProcess getHistoryProcessInstanceDto(String id) {
    return processInstanceDtoMap.get(id);
  }

  public HistoryTask getHistoryTaskDto(String id) {
    return historyTaskDtoMap.get(id);
  }

  private <T> T getOrElse(T obj, T defaultObj) {
    return Objects.isNull(obj) ? defaultObj : obj;
  }
}
