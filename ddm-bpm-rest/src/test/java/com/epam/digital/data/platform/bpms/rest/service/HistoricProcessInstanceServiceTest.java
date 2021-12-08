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

package com.epam.digital.data.platform.bpms.rest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.HistoryProcessInstanceStatus;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.dto.SystemVariablesDto;
import com.epam.digital.data.platform.bpms.rest.mapper.LocalDateTimeMapper;
import com.epam.digital.data.platform.bpms.rest.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.bpms.rest.service.repository.ProcessInstanceHistoricService;
import com.epam.digital.data.platform.bpms.rest.service.repository.VariableInstanceHistoricService;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessExcerptIdVariable;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HistoricProcessInstanceServiceTest {

  @InjectMocks
  private HistoricProcessInstanceService processInstanceService;
  @Mock
  private ProcessInstanceHistoricService processInstanceHistoricService;
  @Mock
  private VariableInstanceHistoricService variableInstanceHistoricService;

  @Spy
  private LocalDateTimeMapper localDateTimeMapper = Mappers.getMapper(LocalDateTimeMapper.class);
  @Spy
  @InjectMocks
  private ProcessInstanceMapper processInstanceMapper = Mappers.getMapper(
      ProcessInstanceMapper.class);

  @Test
  void getHistoryProcessInstancesByParams() {
    var expectedDto = createExpectedDto(HistoryProcessInstanceStatus.ACTIVE);

    var historicProcessInstanceQueryDto = mock(HistoricProcessInstanceQueryDto.class);
    var paginationQueryDto = PaginationQueryDto.builder()
        .maxResults(1)
        .firstResult(2)
        .build();

    mockHistoricProcessInstanceDto(historicProcessInstanceQueryDto, paginationQueryDto,
        expectedDto);

    mockVariables(expectedDto);

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

    when(processInstanceHistoricService.getHistoryProcessInstanceDtos(
        historicProcessInstanceQueryDto, paginationQueryDto)).thenReturn(List.of(mock));
  }

  private void mockProcessInstanceById(HistoryProcessInstanceDto expected) {
    var mock = mockHistoricProcessInstanceDto(expected);

    when(processInstanceHistoricService.getHistoryProcessInstanceDto(expected.getId()))
        .thenReturn(mock);
  }

  private HistoricProcessInstanceDto mockHistoricProcessInstanceDto(
      HistoryProcessInstanceDto expected) {
    var historicProcessInstance = new HistoricProcessInstanceEntity();
    historicProcessInstance.setId(expected.getId());
    historicProcessInstance.setProcessDefinitionId(expected.getProcessDefinitionId());
    historicProcessInstance.setProcessDefinitionName(expected.getProcessDefinitionName());
    historicProcessInstance.setStartTime(
        Date.from(expected.getStartTime().toInstant(ZoneOffset.UTC)));
    historicProcessInstance.setEndTime(Date.from(expected.getEndTime().toInstant(ZoneOffset.UTC)));
    historicProcessInstance.setState(expected.getState().name());

    return HistoricProcessInstanceDto.fromHistoricProcessInstance(historicProcessInstance);
  }

  private void mockVariables(HistoryProcessInstanceDto expected) {
    Map<String, Object> variables = Map.of(
        ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        expected.getProcessCompletionResult(),
        ProcessExcerptIdVariable.SYS_VAR_PROCESS_EXCERPT_ID, expected.getExcerptId());
    when(variableInstanceHistoricService.getSystemVariablesForProcessInstanceIds(expected.getId()))
        .thenReturn(Map.of(expected.getId(), new SystemVariablesDto(variables)));
  }
}
