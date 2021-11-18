package com.epam.digital.data.platform.bpms.rest.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.variable.Variables.stringValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.rest.mapper.LocalDateTimeMapper;
import com.epam.digital.data.platform.bpms.rest.mapper.TaskMapper;
import com.epam.digital.data.platform.bpms.rest.service.ProcessDefinitionImpersonatedService;
import com.epam.digital.data.platform.bpms.rest.service.TaskPropertyService;
import com.epam.digital.data.platform.dso.api.dto.Subject;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Request;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.sub.VariableResource;
import org.camunda.bpm.engine.rest.sub.task.TaskResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

  @InjectMocks
  private TaskServiceImpl taskService;
  @Mock
  private ProcessDefinitionImpersonatedService processDefinitionImpersonatedService;
  @Mock
  private TaskPropertyService taskPropertyService;
  @Mock
  private TaskRestService taskRestService;
  @Spy
  private LocalDateTimeMapper localDateTimeMapper = Mappers.getMapper(LocalDateTimeMapper.class);
  @Spy
  @InjectMocks
  private TaskMapper taskMapper = Mappers.getMapper(TaskMapper.class);

  @Mock
  private TaskResource taskResource;
  @Mock
  private VariableResource variableResource;
  @Mock
  private Request request;

  @Test
  void getTaskById_emptyFormVariables() {
    var processDefinitionId = "processDefinitionId";
    var processDefinitionName = "processDefinitionName";
    var processDefinition = mock(ProcessDefinition.class);
    when(processDefinition.getName()).thenReturn(processDefinitionName);

    var taskId = "taskId";
    var taskDto = new TaskDto();
    taskDto.setId(taskId);
    ReflectionTestUtils.setField(taskDto, "processDefinitionId", processDefinitionId);

    when(taskRestService.getTask(taskId)).thenReturn(taskResource);
    when(taskResource.getTask(request)).thenReturn(taskDto);
    when(processDefinitionImpersonatedService.getProcessDefinition(processDefinitionId))
        .thenReturn(processDefinition);

    var result = taskService.getTaskById(taskId, request);

    assertThat(result)
        .hasFieldOrPropertyWithValue("id", taskId)
        .hasFieldOrPropertyWithValue("processDefinitionId", processDefinitionId)
        .hasFieldOrPropertyWithValue("processDefinitionName", processDefinitionName)
        .hasFieldOrPropertyWithValue("eSign", false)
        .hasFieldOrPropertyWithValue("signatureValidationPack", Set.of())
        .hasFieldOrPropertyWithValue("formVariables", Map.of());
  }

  @Test
  void getTaskById() {
    var processDefinitionId = "processDefinitionId";
    var processDefinitionName = "processDefinitionName";
    var processDefinition = mock(ProcessDefinition.class);
    when(processDefinition.getName()).thenReturn(processDefinitionName);

    var taskId = "taskId";
    var taskDto = new TaskDto();
    taskDto.setId(taskId);
    ReflectionTestUtils.setField(taskDto, "processDefinitionId", processDefinitionId);

    when(taskRestService.getTask(taskId)).thenReturn(taskResource);
    when(taskResource.getTask(request)).thenReturn(taskDto);
    when(processDefinitionImpersonatedService.getProcessDefinition(processDefinitionId))
        .thenReturn(processDefinition);
    when(taskResource.getVariables()).thenReturn(variableResource);
    when(variableResource.getVariables(true)).thenReturn(Map.of(
        "var1", VariableValueDto.fromTypedValue(stringValue("value1")),
        "var3", VariableValueDto.fromTypedValue(stringValue("value3"))
    ));

    when(taskPropertyService.getTaskProperty(taskId)).thenReturn(
        Map.of(
            "eSign", "true",
            "formVariables", "var1,var2",
            Subject.ENTREPRENEUR.name(), "true",
            Subject.LEGAL.name(), "true",
            Subject.INDIVIDUAL.name(), "false"
        ));

    var result = taskService.getTaskById(taskId, request);

    assertThat(result)
        .hasFieldOrPropertyWithValue("id", taskId)
        .hasFieldOrPropertyWithValue("processDefinitionId", processDefinitionId)
        .hasFieldOrPropertyWithValue("processDefinitionName", processDefinitionName)
        .hasFieldOrPropertyWithValue("eSign", true)
        .hasFieldOrPropertyWithValue("signatureValidationPack",
            Set.of(Subject.ENTREPRENEUR, Subject.LEGAL))
        .hasFieldOrPropertyWithValue("formVariables", Map.of("var1", "value1"));
  }
}
