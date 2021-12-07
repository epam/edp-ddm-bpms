package com.epam.digital.data.platform.bpms.rest.service.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import java.util.List;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessInstanceHistoricServiceTest {

  @InjectMocks
  private ProcessInstanceHistoricService service;
  @Mock
  private ProcessEngine processEngine;
  @Mock
  private HistoryService historyService;

  @Test
  void getHistoryProcessInstanceDtos() {
    var queryDtoMock = mock(HistoricProcessInstanceQueryDto.class);
    var paginationQueryDto = PaginationQueryDto.builder()
        .firstResult(1)
        .maxResults(2)
        .build();

    var queryMock = mock(HistoricProcessInstanceQuery.class);
    when(queryDtoMock.toQuery(processEngine)).thenReturn(queryMock);

    var expected = new HistoricProcessInstanceEntity();
    expected.setId("id");
    when(queryMock.listPage(1, 2)).thenReturn(List.of(expected));

    var result = service.getHistoryProcessInstanceDtos(queryDtoMock, paginationQueryDto);

    assertThat(result).hasSize(1)
        .element(0).hasFieldOrPropertyWithValue("id", "id");
  }

  @Test
  void getHistoryProcessInstanceDto() {
    var id = "id";

    var queryMock = mock(HistoricProcessInstanceQuery.class);
    when(historyService.createHistoricProcessInstanceQuery()).thenReturn(queryMock);
    when(queryMock.processInstanceId(id)).thenReturn(queryMock);

    var expected = new HistoricProcessInstanceEntity();
    expected.setId("id");
    when(queryMock.singleResult()).thenReturn(expected);

    var result = service.getHistoryProcessInstanceDto(id);

    assertThat(result).hasFieldOrPropertyWithValue("id", "id");
  }
}
