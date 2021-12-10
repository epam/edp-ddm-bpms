package com.epam.digital.data.platform.bpms.rest.service.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.rest.dto.PaginationQueryDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.Query;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskRuntimeServiceTest {

  @InjectMocks
  private TaskRuntimeService service;
  @Mock
  private ProcessEngine processEngine;
  @Mock
  private TaskService taskService;
  @Mock
  private RepositoryService repositoryService;
  @Mock
  private ObjectMapper objectMapper;

  @Test
  void getTasksByParams() {
    var queryDtoMock = mock(TaskQueryDto.class);
    var paginationQueryDto = PaginationQueryDto.builder()
        .firstResult(1)
        .maxResults(2)
        .build();

    var queryMock = mock(TaskQuery.class);
    when(queryDtoMock.toQuery(processEngine)).thenReturn(queryMock);

    var expected = new TaskEntity();
    expected.setId("id");
    when(queryMock.listPage(1, 2)).thenReturn(List.of(expected));

    var result = service.getTasksByParams(queryDtoMock, paginationQueryDto);

    assertThat(result).hasSize(1)
        .element(0).hasFieldOrPropertyWithValue("id", "id");
  }

  @Test
  void getTaskById() {
    var taskId = "id";

    var query = mock(TaskQuery.class);
    when(taskService.createTaskQuery()).thenReturn(query);
    when(query.taskId(taskId)).thenReturn(query);
    when(query.initializeFormKeys()).thenReturn(query);

    var task = new TaskEntity();
    task.setId(taskId);
    when(query.list()).thenReturn(List.of(task)).thenReturn(List.of());

    assertThat(service.getTaskById(taskId)).isNotEmpty().get()
        .hasFieldOrPropertyWithValue("id", "id");

    assertThat(service.getTaskById(taskId)).isEmpty();
  }

  @Test
  void getVariables() {
    var taskId = "id";

    var variables = new VariableMapImpl();
    variables.put("key", "value");
    when(taskService.getVariables(taskId)).thenReturn(variables);

    var result = service.getVariables(taskId);

    assertThat(result).hasSize(1)
        .extractingByKey("key").extracting(VariableValueDto::getValue).isEqualTo("value");
  }

  @Test
  @SuppressWarnings("unchecked")
  void getTaskProperty() {
    var taskQuery = mock(TaskQuery.class);
    when(taskService.createTaskQuery()).thenReturn(taskQuery);
    when(taskQuery.taskId("id")).thenReturn(taskQuery);

    var task = new TaskEntity();
    task.setProcessDefinitionId("processDefinitionId");
    task.setTaskDefinitionKey("taskDefinitionKey");
    when(taskQuery.singleResult()).thenReturn(task).thenReturn(null);

    var bpmnModelInstance = mock(BpmnModelInstance.class);
    when(repositoryService.getBpmnModelInstance("processDefinitionId")).thenReturn(
        bpmnModelInstance);

    var userTask = mock(UserTask.class);
    when(bpmnModelInstance.getModelElementsByType(UserTask.class)).thenReturn(List.of(userTask));

    var extensionElements = mock(ExtensionElements.class);
    when(userTask.getExtensionElements()).thenReturn(extensionElements);
    when(userTask.getId()).thenReturn("taskDefinitionKey");

    Query<ModelElementInstance> elementsQuery = mock(Query.class);
    when(extensionElements.getElementsQuery()).thenReturn(elementsQuery);

    Query<CamundaProperties> camundaPropertiesQuery = mock(Query.class);
    when(elementsQuery.filterByType(CamundaProperties.class)).thenReturn(camundaPropertiesQuery);

    var camundaProperties = mock(CamundaProperties.class);
    when(camundaPropertiesQuery.list()).thenReturn(List.of(camundaProperties));

    var camundaProperty = mock(CamundaProperty.class);
    when(camundaProperties.getCamundaProperties()).thenReturn(List.of(camundaProperty));

    when(camundaProperty.getCamundaName()).thenReturn("name");
    when(camundaProperty.getCamundaValue()).thenReturn("value");

    assertThat(service.getTaskProperty("id")).hasSize(1)
        .containsEntry("name", "value");

    assertThat(service.getTaskProperty("id")).isEmpty();
  }

  @Test
  void completeTaskWithVariablesInReturn() {
    var completeTaskDto = new CompleteTaskDto();
    completeTaskDto.setWithVariablesInReturn(true);

    when(taskService.completeWithVariablesInReturn("taskId", null, false))
        .thenReturn(mock(VariableMap.class));

    service.completeTask("taskId", completeTaskDto);

    verify(taskService, never()).complete(any(), any());
    verify(taskService).completeWithVariablesInReturn("taskId", null, false);
  }

  @Test
  void completeTask() {
    var completeTaskDto = new CompleteTaskDto();
    completeTaskDto.setWithVariablesInReturn(false);
    service.completeTask("taskId", completeTaskDto);

    verify(taskService).complete("taskId", null);
    verify(taskService, never()).completeWithVariablesInReturn(any(), any(), anyBoolean());
  }
}
