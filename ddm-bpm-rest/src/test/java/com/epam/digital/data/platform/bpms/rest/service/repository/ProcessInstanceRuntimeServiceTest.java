package com.epam.digital.data.platform.bpms.rest.service.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import java.util.List;
import org.camunda.bpm.engine.ProcessEngine;
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

}
