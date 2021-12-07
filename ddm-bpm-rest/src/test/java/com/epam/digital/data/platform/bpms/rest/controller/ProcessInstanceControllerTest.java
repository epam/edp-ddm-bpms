package com.epam.digital.data.platform.bpms.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.ProcessInstanceDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.service.ProcessInstanceService;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessInstanceControllerTest {

  @InjectMocks
  private ProcessInstanceController processInstanceController;
  @Mock
  private ProcessInstanceService processInstanceService;

  @Test
  void getByParams() {
    var queryDto = mock(ProcessInstanceQueryDto.class);
    var paginationDto = mock(PaginationQueryDto.class);

    List<ProcessInstanceDto> expectedList = List.of();
    when(processInstanceService.getProcessInstancesByParams(queryDto, paginationDto))
        .thenReturn(expectedList);

    var result = processInstanceController.getByParams(queryDto, paginationDto);
    assertThat(result).isSameAs(expectedList);
  }
}
