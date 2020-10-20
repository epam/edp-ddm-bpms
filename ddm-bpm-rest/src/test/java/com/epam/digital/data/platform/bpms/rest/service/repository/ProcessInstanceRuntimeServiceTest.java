package com.epam.digital.data.platform.bpms.rest.service.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import java.util.List;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
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
    var processInstanceId = "processInstanceId";
    var rootProcessInstanceId = "rootProcessInstanceId";
    var executionEntity = new ExecutionEntity();
    executionEntity.setId(rootProcessInstanceId);
    executionEntity.setRootProcessInstanceId(rootProcessInstanceId);

    var query = mock(ProcessInstanceQuery.class);
    when(runtimeService.createProcessInstanceQuery()).thenReturn(query);
    when(query.superProcessInstanceId(processInstanceId)).thenReturn(query);
    when(query.list()).thenReturn(List.of(executionEntity));

    var query2 = mock(ProcessInstanceQuery.class);
    when(runtimeService.createProcessInstanceQuery()).thenReturn(query);
    when(query.superProcessInstanceId(rootProcessInstanceId)).thenReturn(query2);
    when(query2.list()).thenReturn(List.of());

    var result = service.getCallActivityProcessInstances(processInstanceId);

    assertThat(result.size()).isOne();
    assertThat(result.get(0).getId()).isEqualTo(rootProcessInstanceId);
  }
}
