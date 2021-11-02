package com.epam.digital.data.platform.bpms.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.StartFormQueryDto;
import com.epam.digital.data.platform.bpms.service.BatchFormService;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StartFormControllerTest {

  @InjectMocks
  private StartFormController startFormController;
  @Mock
  private BatchFormService batchFormService;

  @Test
  public void getTaskProperty() {
    var startFormQueryDto = StartFormQueryDto.builder()
        .processDefinitionIdIn(List.of("process-definition")).build();
    var expected = new HashMap<String, String>();
    when(
        batchFormService.getStartFormKeys(startFormQueryDto.getProcessDefinitionIdIn())).thenReturn(
        expected);

    var result = startFormController.getStartFormMap(startFormQueryDto);

    verify(batchFormService).getStartFormKeys(startFormQueryDto.getProcessDefinitionIdIn());
    assertThat(result).isSameAs(expected);
  }
}
