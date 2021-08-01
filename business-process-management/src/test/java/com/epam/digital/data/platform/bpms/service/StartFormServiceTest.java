package com.epam.digital.data.platform.bpms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.StartFormQueryDto;
import java.util.List;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StartFormServiceTest {

  @InjectMocks
  private StartFormServiceImpl startFormService;
  @Mock
  private ProcessEngine processEngine;
  @Mock
  private FormService formService;
  @Mock
  private RepositoryService repositoryService;
  @Mock
  private ProcessDefinitionQuery processDefinitionQuery;

  @Mock
  private ProcessDefinition processDefinitionWithStartForm;
  @Mock
  private ProcessDefinition processDefinitionWithoutStartForm;

  @Test
  public void shouldReturnEmptyMapAsTaskHasEmptyProperty() {
    var existedProcessDefinition = "existed";
    var nonExistedProcessDefinition = "notExisted";

    when(processEngine.getFormService()).thenReturn(formService);
    when(processEngine.getRepositoryService()).thenReturn(repositoryService);

    when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
    when(processDefinitionQuery.processDefinitionIdIn(any())).thenReturn(processDefinitionQuery);
    when(processDefinitionQuery.list())
        .thenReturn(List.of(processDefinitionWithStartForm, processDefinitionWithoutStartForm));
    when(processDefinitionWithStartForm.getId()).thenReturn(existedProcessDefinition);
    when(processDefinitionWithStartForm.hasStartFormKey()).thenReturn(true);

    when(formService.getStartFormKey(existedProcessDefinition)).thenReturn("form-key");

    var formKeys = startFormService.getStartFormMap(StartFormQueryDto.builder()
        .processDefinitionIdIn(List.of(existedProcessDefinition, nonExistedProcessDefinition))
        .build());

    assertThat(formKeys).containsEntry(existedProcessDefinition, "form-key");
  }
}
