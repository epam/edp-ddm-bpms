package com.epam.digital.data.platform.bpms.rest.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
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
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
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

  @Spy
  private LocalDateTimeMapper localDateTimeMapper = Mappers.getMapper(LocalDateTimeMapper.class);
  @Spy
  @InjectMocks
  private ProcessInstanceMapper processInstanceMapper = Mappers.getMapper(
      ProcessInstanceMapper.class);

  @Test
  void getHistoryProcessInstancesByParams() {
    var expectedDto = createExpectedDto();

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
    var expectedDto = createExpectedDto();

    mockProcessInstanceById(expectedDto);

    mockVariables(expectedDto);

    var result = processInstanceService.getHistoryProcessInstanceDtoById(expectedDto.getId());

    assertThat(result).isEqualTo(expectedDto);
  }

  private HistoryProcessInstanceDto createExpectedDto() {
    var expectedDto = new HistoryProcessInstanceDto();
    expectedDto.setId("processInstanceId");
    expectedDto.setProcessDefinitionId("processDefinitionId");
    expectedDto.setProcessDefinitionName("processDefinitionName");
    expectedDto.setStartTime(LocalDateTime.of(2021, 11, 11, 15, 58));
    expectedDto.setEndTime(LocalDateTime.of(2021, 11, 11, 15, 59));
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
}
