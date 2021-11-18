package com.epam.digital.data.platform.bpms.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import com.epam.digital.data.platform.bpms.rest.service.ProcessDefinitionService;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessDefinitionControllerTest {

  @InjectMocks
  private ProcessDefinitionController controller;
  @Mock
  private ProcessDefinitionService service;

  @Test
  void getByKey() {
    var key = "key";
    var expected = mock(DdmProcessDefinitionDto.class);

    when(service.getUserProcessDefinitionDtoByKey(key)).thenReturn(expected);

    var result = controller.getByKey(key);

    assertThat(result).isSameAs(expected);
  }

  @Test
  void getByParams() {
    var params = mock(ProcessDefinitionQueryDto.class);
    var expected = mock(DdmProcessDefinitionDto.class);

    when(service.getUserProcessDefinitionDtos(params)).thenReturn(List.of(expected));

    var result = controller.getByParams(params);

    assertThat(result).hasSize(1)
        .element(0).isSameAs(expected);
  }
}
