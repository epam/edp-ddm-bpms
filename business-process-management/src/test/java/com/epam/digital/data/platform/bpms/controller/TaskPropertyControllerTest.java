package com.epam.digital.data.platform.bpms.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.service.TaskPropertyService;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TaskPropertyControllerTest {

  private static final String TEST_ID = "testId";

  @InjectMocks
  private TaskPropertyController taskPropertyController;
  @Mock
  private TaskPropertyService taskPropertyService;

  @Test
  public void getTaskProperty() {
    when(taskPropertyService.getTaskProperty(TEST_ID)).thenReturn(new HashMap<>());

    Map<String, String> taskProperties = taskPropertyController.getTaskProperty(TEST_ID);

    verify(taskPropertyService, times(1)).getTaskProperty(TEST_ID);
    assertThat(taskProperties).isEmpty();
  }
}
