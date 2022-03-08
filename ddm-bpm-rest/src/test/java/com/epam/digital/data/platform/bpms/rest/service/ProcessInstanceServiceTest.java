package com.epam.digital.data.platform.bpms.rest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.DdmProcessInstanceStatus;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.dto.ProcessInstanceExtendedQueryDto;
import com.epam.digital.data.platform.bpms.rest.dto.SystemVariablesDto;
import com.epam.digital.data.platform.bpms.rest.mapper.LocalDateTimeMapper;
import com.epam.digital.data.platform.bpms.rest.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.bpms.rest.service.repository.ProcessDefinitionRepositoryService;
import com.epam.digital.data.platform.bpms.rest.service.repository.ProcessInstanceRuntimeService;
import com.epam.digital.data.platform.bpms.rest.service.repository.TaskRuntimeService;
import com.epam.digital.data.platform.bpms.rest.service.repository.VariableInstanceRuntimeService;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessInstanceServiceTest {

  @InjectMocks
  private ProcessInstanceService processInstanceService;
  @Mock
  private ProcessInstanceRuntimeService processInstanceRuntimeService;
  @Mock
  private VariableInstanceRuntimeService variableInstanceRuntimeService;
  @Mock
  private ProcessDefinitionRepositoryService processDefinitionRepositoryService;
  @Mock
  private TaskRuntimeService taskRuntimeService;

  @Spy
  private LocalDateTimeMapper localDateTimeMapper = Mappers.getMapper(LocalDateTimeMapper.class);
  @Spy
  @InjectMocks
  private ProcessInstanceMapper processInstanceMapper = Mappers.getMapper(
      ProcessInstanceMapper.class);
  @Mock(name = "camundaAdminImpersonation")
  private CamundaImpersonation camundaAdminImpersonation;

  @Captor
  private ArgumentCaptor<TaskQueryDto> taskQueryDtoArgumentCaptor;

  @Test
  void getProcessInstancesByParams() {
    var queryDto = mock(ProcessInstanceExtendedQueryDto.class);
    var paginationQueryDto = PaginationQueryDto.builder().firstResult(1).maxResults(2).build();

    mockQueryProcessInstances(queryDto, paginationQueryDto);
    mockQuerySystemVariablesForProcessInstanceIds();
    mockGetProcessDefinitionNames();
    mockQueryTasks();

    var result = processInstanceService.getProcessInstancesByParams(queryDto, paginationQueryDto);

    assertThat(result).hasSize(1)
        .element(0).isEqualTo(DdmProcessInstanceDto.builder()
            .id("id1")
            .processDefinitionId("processDefinitionId1")
            .processDefinitionName("processDefinitionName1")
            .state(DdmProcessInstanceStatus.PENDING)
            .build());
  }

  private void mockGetProcessDefinitionNames() {
    doAnswer(invocation -> {
      Supplier<?> supplier = invocation.getArgument(0);
      return supplier.get();
    }).when(camundaAdminImpersonation).execute(any());
    when(processDefinitionRepositoryService
        .getProcessDefinitionsNames("processDefinitionId1"))
        .thenReturn(Map.of("processDefinitionId1", "processDefinitionName1"));
  }

  @Test
  void getProcessInstancesByParams_emptyList() {
    var queryDto = mock(ProcessInstanceExtendedQueryDto.class);
    var paginationQueryDto = PaginationQueryDto.builder().firstResult(1).maxResults(2).build();

    when(processInstanceRuntimeService.getProcessInstanceDtos(queryDto, paginationQueryDto))
        .thenReturn(List.of());

    var result = processInstanceService.getProcessInstancesByParams(queryDto, paginationQueryDto);

    assertThat(result).isEmpty();
    verify(processInstanceRuntimeService).getProcessInstanceDtos(queryDto, paginationQueryDto);
    verify(camundaAdminImpersonation, never()).execute(any());
    verify(processDefinitionRepositoryService, never()).getProcessDefinitionsNames(any());
    verify(variableInstanceRuntimeService, never()).getSystemVariablesForProcessInstanceIds(any());
    verify(taskRuntimeService, never()).getTasksByParams(any(), any());
  }

  private void mockQueryProcessInstances(ProcessInstanceQueryDto queryDto,
      PaginationQueryDto paginationQueryDto) {
    var processInstance = new ExecutionEntity();
    processInstance.setId("id1");
    processInstance.setProcessDefinitionId("processDefinitionId1");
    when(processInstanceRuntimeService.getProcessInstanceDtos(queryDto, paginationQueryDto))
        .thenReturn(List.of(ProcessInstanceDto.fromProcessInstance(processInstance)));
  }

  private void mockQuerySystemVariablesForProcessInstanceIds() {
    var systemVariablesDto = new SystemVariablesDto(Map.of());

    when(variableInstanceRuntimeService.getSystemVariablesForProcessInstanceIds("id1"))
        .thenReturn(Map.of("id1", systemVariablesDto));
  }

  private void mockQueryTasks() {
    var taskEntity = new TaskEntity();
    taskEntity.setId("taskId");
    taskEntity.setProcessInstanceId("id1");
    when(taskRuntimeService.getTasksByParams(taskQueryDtoArgumentCaptor.capture(),
        eq(PaginationQueryDto.builder().build())))
        .thenReturn(List.of(TaskDto.fromEntity(taskEntity)));
  }
}
