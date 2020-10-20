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

import com.epam.digital.data.platform.bpms.api.dto.DdmCompletedTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmSignableTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmTaskDto;
import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.rest.service.UserTaskService;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
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
  private UserTaskService taskService;

  @Test
  void getById() {
    var id = "taskId";
    var expected = new DdmSignableTaskDto();
    when(taskService.getTaskById(id)).thenReturn(expected);

    var result = taskController.getById(id);
    assertThat(result).isSameAs(expected);
    verify(taskService).getTaskById(id);
  }

  @Test
  void getByParams() {
    var taskQueryDto = mock(TaskQueryDto.class);
    var paginationQuery = mock(PaginationQueryDto.class);

    List<DdmTaskDto> expected = List.of();
    when(taskService.getTasksByParams(taskQueryDto, paginationQuery)).thenReturn(expected);

    var actual = taskController.getByParams(taskQueryDto, paginationQuery);

    assertThat(actual).isSameAs(expected);
  }

  @Test
  void completeTask() {
    var id = "id";
    var completeTaskDto = mock(CompleteTaskDto.class);

    var expected = DdmCompletedTaskDto.builder().build();
    when(taskService.completeTask(id, completeTaskDto)).thenReturn(expected);

    var actual = taskController.completeTask(id, completeTaskDto);

    assertThat(actual).isSameAs(expected);
  }
}
