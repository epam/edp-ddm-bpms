/*
 * Copyright 2023 EPAM Systems.
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


package com.epam.digital.data.platform.bpms.rest.service.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import java.util.List;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessInstanceRuntimeServiceTest {

  @InjectMocks
  private ProcessInstanceRuntimeService service;
  @Mock
  private ProcessEngine processEngine;
  @Mock
  private RuntimeService runtimeService;

  @Test
  void getProcessInstanceDtos() {
    var queryDtoMock = mock(ProcessInstanceQueryDto.class);
    var paginationQueryDto = PaginationQueryDto.builder()
        .firstResult(1)
        .maxResults(2)
        .build();

    var queryMock = mock(ProcessInstanceQuery.class);
    when(queryDtoMock.toQuery(processEngine)).thenReturn(queryMock);

    var expected = new ExecutionEntity();
    expected.setId("id");
    when(queryMock.listPage(1, 2)).thenReturn(List.of(expected));

    var result = service.getProcessInstanceDtos(queryDtoMock, paginationQueryDto);

    assertThat(result).hasSize(1)
        .element(0).hasFieldOrPropertyWithValue("id", "id");
  }

  @Test
  void getProcessInstance() {
    var query = mock(ProcessInstanceQuery.class);
    when(runtimeService.createProcessInstanceQuery()).thenReturn(query);
    when(query.processInstanceId("processInstanceId")).thenReturn(query);

    var instance = new ExecutionEntity();
    instance.setId("processInstanceId");
    when(query.list()).thenReturn(List.of(instance)).thenReturn(List.of());

    assertThat(service.getProcessInstance("processInstanceId")).isNotEmpty()
        .get().hasFieldOrPropertyWithValue("id", "processInstanceId");

    assertThat(service.getProcessInstance("processInstanceId")).isEmpty();
  }

  @Test
  void getProcessInstance_illegalState() {
    var query = mock(ProcessInstanceQuery.class);
    when(runtimeService.createProcessInstanceQuery()).thenReturn(query);
    when(query.processInstanceId("processInstanceId")).thenReturn(query);
    when(query.list()).thenReturn(List.of(new ExecutionEntity(), new ExecutionEntity()));

    var ex = assertThrows(IllegalStateException.class,
        () -> service.getProcessInstance("processInstanceId"));

    assertThat(ex).isNotNull()
        .hasMessage("Found more than one process instances by id");
  }

  @Test
  void getCallActivityProcessInstances() {
    var subEntityFistLevelId = "subEntityFistLevelId";
    var subEntityFistLevel2Id = "subEntityFistLevel2Id";
    var subEntitySecondLevelId = "subEntitySecondLevelId";
    var rootProcessInstanceId = "rootProcessInstanceId";
    var subEntityFistLevel = new ExecutionEntity();
    subEntityFistLevel.setId(subEntityFistLevelId);
    subEntityFistLevel.setRootProcessInstanceId(rootProcessInstanceId);
    var subEntityFistLevel2 = new ExecutionEntity();
    subEntityFistLevel2.setId(subEntityFistLevel2Id);
    subEntityFistLevel2.setRootProcessInstanceId(rootProcessInstanceId);
    var subEntitySecondLevel = new ExecutionEntity();
    subEntitySecondLevel.setId(subEntitySecondLevelId);
    subEntitySecondLevel.setRootProcessInstanceId(subEntityFistLevel2Id);
    var query = mock(ProcessInstanceQuery.class);
    var queryFirstLevel = mock(ProcessInstanceQuery.class);
    var querySecondLevelEmpty = mock(ProcessInstanceQuery.class);
    var querySecondLevel = mock(ProcessInstanceQuery.class);

    when(runtimeService.createProcessInstanceQuery()).thenReturn(query);
    when(query.superProcessInstanceId(rootProcessInstanceId)).thenReturn(queryFirstLevel);
    when(queryFirstLevel.list()).thenReturn(List.of(subEntityFistLevel, subEntityFistLevel2));
    when(query.superProcessInstanceId(subEntityFistLevelId)).thenReturn(querySecondLevelEmpty);
    when(querySecondLevelEmpty.list()).thenReturn(List.of());
    when(query.superProcessInstanceId(subEntityFistLevel2Id)).thenReturn(querySecondLevel);
    when(querySecondLevel.list()).thenReturn(List.of(subEntitySecondLevel));

    var subInstances = service.getCallActivityProcessInstances(rootProcessInstanceId);
    var ids = subInstances.stream()
        .map(ProcessInstance::getId)
        .collect(Collectors.toList());
    assertThat(subInstances).hasSize(3);
    assertThat(ids).contains(subEntityFistLevelId, subEntityFistLevel2Id, subEntitySecondLevelId);
  }
}
