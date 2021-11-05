package com.epam.digital.data.platform.bpms.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.SignableUserTaskDto;
import com.epam.digital.data.platform.bpms.rest.service.TaskService;
import javax.ws.rs.core.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

  @InjectMocks
  private TaskController taskController;
  @Mock
  private TaskService taskService;

  @Mock
  private Request request;

  @Test
  void getById() {
    var id = "taskId";
    var expected = new SignableUserTaskDto();
    when(taskService.getTaskById(id, request)).thenReturn(expected);

    var result = taskController.getById(id, request);
    assertThat(result).isSameAs(expected);
    verify(taskService).getTaskById(id, request);
  }
}
