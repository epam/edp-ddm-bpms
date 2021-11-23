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

package com.epam.digital.data.platform.bpms.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.service.HistoricProcessInstanceService;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HistoricProcessInstanceControllerTest {

  @InjectMocks
  private HistoricProcessInstanceController historicProcessInstanceController;
  @Mock
  private HistoricProcessInstanceService historicProcessInstanceService;

  @Test
  void getByParams() {
    var historicProcessInstanceQueryDto = mock(HistoricProcessInstanceQueryDto.class);
    var paginationQueryDto = PaginationQueryDto.builder()
        .firstResult(1)
        .maxResults(2)
        .build();

    var expected = mock(HistoryProcessInstanceDto.class);
    when(historicProcessInstanceService.getHistoryProcessInstancesByParams(
        historicProcessInstanceQueryDto, paginationQueryDto)).thenReturn(List.of(expected));

    var result = historicProcessInstanceController.getByParams(historicProcessInstanceQueryDto,
        paginationQueryDto);

    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isSameAs(expected);

    verify(historicProcessInstanceService).getHistoryProcessInstancesByParams(
        historicProcessInstanceQueryDto, paginationQueryDto);
  }

  @Test
  void getById() {
    var id = "id";

    var expected = mock(HistoryProcessInstanceDto.class);
    when(historicProcessInstanceService.getHistoryProcessInstanceDtoById(id))
        .thenReturn(expected);

    var result = historicProcessInstanceController.getById(id);

    assertThat(result).isSameAs(expected);

    verify(historicProcessInstanceService).getHistoryProcessInstanceDtoById(id);
  }
}
