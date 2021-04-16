package com.epam.digital.data.platform.bpms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TaskPropertyServiceTest {

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

  @Test
  public void shouldReturnEmptyMapAsTaskHasEmptyProperty() {
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

    Map<String, String> taskProperties = taskPropertyService.getTaskProperty(ID);

    assertThat(taskProperties).isEmpty();
  }
}
