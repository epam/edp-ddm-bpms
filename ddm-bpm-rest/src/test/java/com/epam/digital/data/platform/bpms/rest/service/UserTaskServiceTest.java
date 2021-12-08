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
import static org.camunda.bpm.engine.variable.Variables.stringValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.mapper.LocalDateTimeMapper;
import com.epam.digital.data.platform.bpms.rest.mapper.TaskMapper;
import com.epam.digital.data.platform.bpms.rest.service.repository.ProcessDefinitionRepositoryService;
import com.epam.digital.data.platform.bpms.rest.service.repository.TaskRuntimeService;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonation;
import com.epam.digital.data.platform.dso.api.dto.Subject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import javax.ws.rs.core.Response.Status;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserTaskServiceTest {

  @InjectMocks
  private UserTaskService service;
  @Mock
  private TaskRuntimeService taskRuntimeService;
  @Mock
  private ProcessDefinitionRepositoryService processDefinitionRepositoryService;
  @Spy
  private LocalDateTimeMapper localDateTimeMapper = Mappers.getMapper(LocalDateTimeMapper.class);
  @Spy
  @InjectMocks
  private TaskMapper taskMapper = Mappers.getMapper(TaskMapper.class);

  @Mock
  private CamundaImpersonation camundaAdminImpersonation;

  @Test
  void getTaskById_emptyFormVariables() {
    var processDefinitionId = "processDefinitionId";
    var processDefinitionName = "processDefinitionName";
    var processDefinition = mock(ProcessDefinition.class);
    when(processDefinition.getName()).thenReturn(processDefinitionName);

    var taskId = "taskId";
    var taskDto = new TaskDto();
    taskDto.setId(taskId);
    ReflectionTestUtils.setField(taskDto, "processDefinitionId", processDefinitionId);

    when(taskRuntimeService.getTaskById(taskId)).thenReturn(Optional.of(taskDto));
    when(processDefinitionRepositoryService.getProcessDefinitionById(processDefinitionId))
        .thenReturn(processDefinition);

    doAnswer(invocation -> {
      Supplier<?> supplier = invocation.getArgument(0);
      return supplier.get();
    }).when(camundaAdminImpersonation).execute(any());

    var result = service.getTaskById(taskId);

    assertThat(result)
        .hasFieldOrPropertyWithValue("id", taskId)
        .hasFieldOrPropertyWithValue("processDefinitionId", processDefinitionId)
        .hasFieldOrPropertyWithValue("processDefinitionName", processDefinitionName)
        .hasFieldOrPropertyWithValue("eSign", false)
        .hasFieldOrPropertyWithValue("signatureValidationPack", Set.of())
        .hasFieldOrPropertyWithValue("formVariables", Map.of());
  }

  @Test
  void getTaskById_noTaskFound() {
    var taskId = "taskId";

    when(taskRuntimeService.getTaskById(taskId)).thenReturn(Optional.empty());

    var ex = assertThrows(RestException.class, () -> service.getTaskById(taskId));

    assertThat(ex)
        .hasFieldOrPropertyWithValue("message", "No matching task with id taskId")
        .hasFieldOrPropertyWithValue("status", Status.NOT_FOUND);
  }

  @Test
  void getTaskById() {
    var processDefinitionId = "processDefinitionId";
    var processDefinitionName = "processDefinitionName";
    var processDefinition = mock(ProcessDefinition.class);
    when(processDefinition.getName()).thenReturn(processDefinitionName);

    var taskId = "taskId";
    var taskDto = new TaskDto();
    taskDto.setId(taskId);
    ReflectionTestUtils.setField(taskDto, "processDefinitionId", processDefinitionId);

    when(taskRuntimeService.getTaskById(taskId)).thenReturn(Optional.of(taskDto));
    when(processDefinitionRepositoryService.getProcessDefinitionById(processDefinitionId))
        .thenReturn(processDefinition);
    when(taskRuntimeService.getVariables(taskId)).thenReturn(Map.of(
        "var1", VariableValueDto.fromTypedValue(stringValue("value1")),
        "var3", VariableValueDto.fromTypedValue(stringValue("value3"))
    ));

    doAnswer(invocation -> {
      Supplier<?> supplier = invocation.getArgument(0);
      return supplier.get();
    }).when(camundaAdminImpersonation).execute(any());

    when(taskRuntimeService.getTaskProperty(taskId)).thenReturn(
        Map.of(
            "eSign", "true",
            "formVariables", "var1,var2",
            Subject.ENTREPRENEUR.name(), "true",
            Subject.LEGAL.name(), "true",
            Subject.INDIVIDUAL.name(), "false"
        ));

    var result = service.getTaskById(taskId);

    assertThat(result)
        .hasFieldOrPropertyWithValue("id", taskId)
        .hasFieldOrPropertyWithValue("processDefinitionId", processDefinitionId)
        .hasFieldOrPropertyWithValue("processDefinitionName", processDefinitionName)
        .hasFieldOrPropertyWithValue("eSign", true)
        .hasFieldOrPropertyWithValue("signatureValidationPack",
            Set.of(Subject.ENTREPRENEUR, Subject.LEGAL))
        .hasFieldOrPropertyWithValue("formVariables", Map.of("var1", "value1"));
  }

  @Test
  void getTasksByParams() {
    var queryDto = mock(TaskQueryDto.class);
    var paginationQueryDto = PaginationQueryDto.builder().firstResult(1).maxResults(2).build();

    var task = new TaskEntity();
    task.setId("id");
    task.setProcessDefinitionId("processDefinitionId");
    var taskDto = TaskDto.fromEntity(task);
    when(taskRuntimeService.getTasksByParams(queryDto, paginationQueryDto))
        .thenReturn(List.of(taskDto)).thenReturn(List.of());

    doAnswer(invocation -> {
      Supplier<?> supplier = invocation.getArgument(0);
      return supplier.get();
    }).when(camundaAdminImpersonation).execute(any());

    when(processDefinitionRepositoryService.getProcessDefinitionsNames("processDefinitionId"))
        .thenReturn(Map.of("processDefinitionId", "processDefinitionName"));

    var result = service.getTasksByParams(queryDto, paginationQueryDto);

    assertThat(result).hasSize(1).element(0)
        .hasFieldOrPropertyWithValue("id", "id")
        .hasFieldOrPropertyWithValue("processDefinitionId", "processDefinitionId")
        .hasFieldOrPropertyWithValue("processDefinitionName", "processDefinitionName");

    assertThat(service.getTasksByParams(queryDto, paginationQueryDto)).isEmpty();

    verify(taskRuntimeService, times(2)).getTasksByParams(queryDto, paginationQueryDto);
    verify(camundaAdminImpersonation).execute(any());
    verify(processDefinitionRepositoryService).getProcessDefinitionsNames("processDefinitionId");
  }
}
