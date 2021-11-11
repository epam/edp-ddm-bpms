package com.epam.digital.data.platform.bpms.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.rest.service.TaskPropertyService;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskPropertyControllerTest {

  private static final String TEST_ID = "testId";

  @InjectMocks
  private TaskPropertyController taskPropertyController;
  @Mock
  private TaskPropertyService taskPropertyService;

  @Test
  void getTaskProperty() {
    when(taskPropertyService.getTaskProperty(TEST_ID)).thenReturn(new HashMap<>());

    Map<String, String> taskProperties = taskPropertyController.getTaskProperty(TEST_ID);

    verify(taskPropertyService, times(1)).getTaskProperty(TEST_ID);
    assertThat(taskProperties).isEmpty();
  }
}
