package com.epam.digital.data.platform.bpms.rest.service.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessDefinitionRepositoryServiceTest {

  @InjectMocks
  private ProcessDefinitionRepositoryService service;
  @Mock
  private RepositoryService repositoryService;
  @Mock
  private ProcessEngine processEngine;

  @Test
  void getProcessDefinitionsNames() {
    var processDefinitionIds = new String[]{"processDefinitionId1", "processDefinitionId2"};

    var query = mock(ProcessDefinitionQuery.class);
    when(repositoryService.createProcessDefinitionQuery()).thenReturn(query);
    when(query.processDefinitionIdIn(processDefinitionIds)).thenReturn(query);

    var processDefinition1 = new ProcessDefinitionEntity();
    processDefinition1.setId("processDefinitionId1");
    processDefinition1.setName(null);

    var processDefinition2 = new ProcessDefinitionEntity();
    processDefinition2.setId("processDefinitionId2");
    processDefinition2.setName("processDefinitionName2");

    when(query.list()).thenReturn(List.of(processDefinition1, processDefinition2));

    var result = service.getProcessDefinitionsNames(processDefinitionIds);

    assertThat(result).hasSize(1)
        .containsEntry("processDefinitionId2", "processDefinitionName2");
  }

  @Test
  void getProcessDefinitionDtoByKey() {
    var key = "processDefinitionKey";

    var query = mock(ProcessDefinitionQuery.class);
    when(repositoryService.createProcessDefinitionQuery()).thenReturn(query);
    when(query.processDefinitionKey(key)).thenReturn(query);
    when(query.withoutTenantId()).thenReturn(query);
    when(query.latestVersion()).thenReturn(query);

    var processDefinition = new ProcessDefinitionEntity();
    processDefinition.setId("processDefinitionId");
    processDefinition.setName("processDefinitionName");
    when(query.list()).thenReturn(List.of(processDefinition));

    var result = service.getProcessDefinitionDtoByKey(key);

    assertThat(result).isNotEmpty()
        .get().hasFieldOrPropertyWithValue("id", "processDefinitionId")
        .hasFieldOrPropertyWithValue("name", "processDefinitionName");
  }

  @Test
  void getProcessDefinitionDtos() {
    var queryDto = mock(ProcessDefinitionQueryDto.class);

    var query = mock(ProcessDefinitionQuery.class);
    when(queryDto.toQuery(processEngine)).thenReturn(query);

    var processDefinition = new ProcessDefinitionEntity();
    processDefinition.setId("processDefinitionId");
    processDefinition.setName("processDefinitionName");
    when(query.list()).thenReturn(List.of(processDefinition));

    var result = service.getProcessDefinitionDtos(queryDto);

    assertThat(result).hasSize(1).element(0)
        .hasFieldOrPropertyWithValue("id", "processDefinitionId")
        .hasFieldOrPropertyWithValue("name", "processDefinitionName");
  }

  @Test
  void getProcessDefinitionById() {
    var id = "id";

    var expectedDto = new ProcessDefinitionEntity();
    when(repositoryService.getProcessDefinition(id)).thenReturn(expectedDto);

    var result = service.getProcessDefinitionById(id);

    assertThat(result).isSameAs(expectedDto);
  }
}
