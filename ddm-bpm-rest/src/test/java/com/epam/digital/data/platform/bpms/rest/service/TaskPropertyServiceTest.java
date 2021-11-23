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

package com.epam.digital.data.platform.bpms.rest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.rest.service.impl.TaskPropertyServiceImpl;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonation;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonationFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskPropertyServiceTest {

  private static final String ID = "id";

  @InjectMocks
  private TaskPropertyServiceImpl taskPropertyService;
  @Mock
  private ProcessEngine processEngine;
  @Mock
  private TaskService taskService;
  @Mock
  private TaskQuery taskQuery;
  @Mock
  private Task task;
  @Mock
  private RepositoryService repositoryService;
  @Mock
  private BpmnModelInstance bpmnModelInstance;
  @Mock
  private UserTask userTask;
  @Mock
  private CamundaImpersonationFactory camundaImpersonationFactory;
  @Mock
  private CamundaImpersonation camundaImpersonation;

  @Test
  void shouldThrowExceptionIfImpersonationIsEmpty() {
    when(camundaImpersonationFactory.getCamundaImpersonation()).thenReturn(Optional.empty());

    assertThrows(IllegalStateException.class, () -> taskPropertyService.getTaskProperty(ID));
  }

  @Test
  void shouldReturnEmptyMapAsTaskHasEmptyProperty() {
    Collection<UserTask> userTasks = new ArrayList<>();
    userTasks.add(userTask);
    when(processEngine.getTaskService()).thenReturn(taskService);
    when(taskService.createTaskQuery()).thenReturn(taskQuery);
    when(taskQuery.taskId(ID)).thenReturn(taskQuery);
    when(processEngine.getTaskService()).thenReturn(taskService);
    when(taskService.createTaskQuery()).thenReturn(taskQuery);
    when(taskQuery.taskId(ID)).thenReturn(taskQuery);
    when(taskQuery.singleResult()).thenReturn(task);
    when(processEngine.getRepositoryService()).thenReturn(repositoryService);
    when(task.getProcessDefinitionId()).thenReturn(ID);
    when(repositoryService.getBpmnModelInstance(ID)).thenReturn(bpmnModelInstance);
    when(bpmnModelInstance.getModelElementsByType(UserTask.class)).thenReturn(userTasks);
    when(userTask.getId()).thenReturn(ID);
    when(task.getTaskDefinitionKey()).thenReturn(ID);
    when(camundaImpersonationFactory.getCamundaImpersonation())
        .thenReturn(Optional.of(camundaImpersonation));

    Map<String, String> taskProperties = taskPropertyService.getTaskProperty(ID);

    assertThat(taskProperties).isEmpty();
    verify(camundaImpersonation).impersonate();
    verify(camundaImpersonation).revertToSelf();
  }
}
