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

package com.epam.digital.data.platform.bpms.rest.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.HistoryProcessInstanceStatus;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.mapper.LocalDateTimeMapper;
import com.epam.digital.data.platform.bpms.rest.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.dataaccessor.sysvar.Constants;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessExcerptIdVariable;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.history.HistoricProcessInstanceRestService;
import org.camunda.bpm.engine.rest.sub.history.HistoricProcessInstanceResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HistoricProcessInstanceServiceImplTest {

  @InjectMocks
  private HistoricProcessInstanceServiceImpl processInstanceService;
  @Mock
  private HistoricProcessInstanceRestService historicProcessInstanceRestService;
  @Mock
  private HistoryService historyService;
  @Mock
  private TaskRestService taskRestService;

  @Spy
  private LocalDateTimeMapper localDateTimeMapper = Mappers.getMapper(LocalDateTimeMapper.class);
  @Spy
  @InjectMocks
  private ProcessInstanceMapper processInstanceMapper = Mappers.getMapper(
      ProcessInstanceMapper.class);

  @Test
  void getHistoryProcessInstancesByParams() {
    var expectedDto = createExpectedDto(HistoryProcessInstanceStatus.PENDING);

    var historicProcessInstanceQueryDto = mock(HistoricProcessInstanceQueryDto.class);
    var paginationQueryDto = PaginationQueryDto.builder()
        .maxResults(1)
        .firstResult(2)
        .build();

    mockHistoricProcessInstanceDto(historicProcessInstanceQueryDto, paginationQueryDto,
        expectedDto);

    mockVariables(expectedDto);

    mockTasks(expectedDto);

    var result = processInstanceService.getHistoryProcessInstancesByParams(
        historicProcessInstanceQueryDto, paginationQueryDto);

    assertThat(result).hasSize(1)
        .contains(expectedDto);
  }

  @Test
  void getHistoryProcessInstancesByParams_completed() {
    var expectedDto = createExpectedDto(HistoryProcessInstanceStatus.COMPLETED);

    var historicProcessInstanceQueryDto = mock(HistoricProcessInstanceQueryDto.class);
    var paginationQueryDto = PaginationQueryDto.builder()
        .maxResults(1)
        .firstResult(2)
        .build();

    mockHistoricProcessInstanceDto(historicProcessInstanceQueryDto, paginationQueryDto,
        expectedDto);

    mockVariables(expectedDto);

    mockTasks(expectedDto);

    var result = processInstanceService.getHistoryProcessInstancesByParams(
        historicProcessInstanceQueryDto, paginationQueryDto);

    assertThat(result).hasSize(1)
        .contains(expectedDto);
  }

  @Test
  void getHistoryProcessInstanceDtoById() {
    var expectedDto = createExpectedDto(HistoryProcessInstanceStatus.COMPLETED);

    mockProcessInstanceById(expectedDto);

    mockVariables(expectedDto);

    var result = processInstanceService.getHistoryProcessInstanceDtoById(expectedDto.getId());

    assertThat(result).isEqualTo(expectedDto);
  }

  private HistoryProcessInstanceDto createExpectedDto(HistoryProcessInstanceStatus state) {
    var expectedDto = new HistoryProcessInstanceDto();
    expectedDto.setId("processInstanceId");
    expectedDto.setProcessDefinitionId("processDefinitionId");
    expectedDto.setProcessDefinitionName("processDefinitionName");
    expectedDto.setStartTime(LocalDateTime.of(2021, 11, 11, 15, 58));
    expectedDto.setEndTime(LocalDateTime.of(2021, 11, 11, 15, 59));
    expectedDto.setState(state);
    expectedDto.setProcessCompletionResult("completed status");
    expectedDto.setExcerptId("excerpt id");
    return expectedDto;
  }

  private void mockHistoricProcessInstanceDto(
      HistoricProcessInstanceQueryDto historicProcessInstanceQueryDto,
      PaginationQueryDto paginationQueryDto, HistoryProcessInstanceDto expected) {
    var mock = mockHistoricProcessInstanceDto(expected);

    when(historicProcessInstanceRestService.queryHistoricProcessInstances(
        historicProcessInstanceQueryDto, paginationQueryDto.getFirstResult(),
        paginationQueryDto.getMaxResults())).thenReturn(List.of(mock));
  }

  private void mockProcessInstanceById(HistoryProcessInstanceDto expected) {
    HistoricProcessInstanceDto mock = mockHistoricProcessInstanceDto(expected);

    var resource = mock(HistoricProcessInstanceResource.class);
    when(
        historicProcessInstanceRestService.getHistoricProcessInstance(expected.getId())).thenReturn(
        resource);
    when(resource.getHistoricProcessInstance()).thenReturn(mock);
  }

  private HistoricProcessInstanceDto mockHistoricProcessInstanceDto(
      HistoryProcessInstanceDto expected) {
    var mock = mock(HistoricProcessInstanceDto.class);
    when(mock.getId()).thenReturn(expected.getId());
    when(mock.getProcessDefinitionId()).thenReturn(expected.getProcessDefinitionId());
    when(mock.getProcessDefinitionName()).thenReturn(expected.getProcessDefinitionName());
    when(mock.getStartTime()).thenReturn(
        Date.from(expected.getStartTime().toInstant(ZoneOffset.UTC)));
    when(mock.getEndTime()).thenReturn(Date.from(expected.getEndTime().toInstant(ZoneOffset.UTC)));
    var state = expected.getState();
    when(mock.getState()).thenReturn(state.equals(HistoryProcessInstanceStatus.PENDING)
        ? HistoryProcessInstanceStatus.ACTIVE.name() : state.name());
    return mock;
  }

  private void mockVariables(HistoryProcessInstanceDto expected) {
    var variableQuery = mock(HistoricVariableInstanceQuery.class);
    when(historyService.createHistoricVariableInstanceQuery()).thenReturn(variableQuery);
    when(variableQuery.processInstanceIdIn(expected.getId())).thenReturn(variableQuery);
    when(variableQuery.variableNameLike(Constants.SYS_VAR_PREFIX_LIKE)).thenReturn(variableQuery);

    var variable1 = mockVariableInstanceEntity(expected.getId(), "sys-var-var-1", null);
    var variable2 = mockVariableInstanceEntity(expected.getId(),
        ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        expected.getProcessCompletionResult());
    var variable3 = mockVariableInstanceEntity(expected.getId(),
        ProcessExcerptIdVariable.SYS_VAR_PROCESS_EXCERPT_ID, expected.getExcerptId());
    when(variableQuery.list()).thenReturn(List.of(variable1, variable2, variable3));
  }

  private HistoricVariableInstanceEntity mockVariableInstanceEntity(String processInstanceId,
      String name, String value) {
    var variableInstance = mock(HistoricVariableInstanceEntity.class);
    lenient().when(variableInstance.getProcessInstanceId()).thenReturn(processInstanceId);
    lenient().when(variableInstance.getName()).thenReturn(name);
    when(variableInstance.getValue()).thenReturn(value);
    return variableInstance;
  }

  private void mockTasks(HistoryProcessInstanceDto expected) {
    if (!HistoryProcessInstanceStatus.PENDING.equals(expected.getState())) {
      return;
    }

    var taskDto = mock(TaskDto.class);
    when(taskDto.getProcessInstanceId()).thenReturn(expected.getId());
    when(taskRestService.queryTasks(any(), isNull(), isNull())).thenReturn(List.of(taskDto));
  }
}
