package com.epam.digital.data.platform.bpms.rest.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import com.epam.digital.data.platform.bpms.engine.service.BatchFormService;
import com.epam.digital.data.platform.bpms.rest.mapper.ProcessDefinitionMapper;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response.Status;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessDefinitionServiceImplTest {

  @InjectMocks
  private ProcessDefinitionServiceImpl processDefinitionService;
  @Mock
  private ProcessEngine processEngine;
  @Mock
  private BatchFormService batchFormService;
  @Spy
  private ProcessDefinitionMapper processDefinitionMapper = Mappers.getMapper(
      ProcessDefinitionMapper.class);

  @Test
  void getUserProcessDefinitionDtoByKey() {
    var key = "key";
    var expectedResult = createUserProcessDefinitionDto(key);

    mockProcessInstanceByKey(expectedResult);
    mockFormKeys(expectedResult);

    var result = processDefinitionService.getUserProcessDefinitionDtoByKey(key);

    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void getUserProcessDefinitionDtoByKey_throwsRestException() {
    var key = "key";

    var repoService = mock(RepositoryService.class);
    when(processEngine.getRepositoryService()).thenReturn(repoService);

    var query = mock(ProcessDefinitionQuery.class);
    when(repoService.createProcessDefinitionQuery()).thenReturn(query);
    when(query.latestVersion()).thenReturn(query);
    when(query.withoutTenantId()).thenReturn(query);
    when(query.processDefinitionKey(key)).thenReturn(query);
    when(query.list()).thenReturn(List.of());

    var ex = assertThrows(RestException.class,
        () -> processDefinitionService.getUserProcessDefinitionDtoByKey(key));

    assertThat(ex)
        .hasFieldOrPropertyWithValue("message",
            "No matching process definition with key: key and no tenant-id")
        .hasFieldOrPropertyWithValue("status", Status.NOT_FOUND);
  }

  @Test
  void getUserProcessDefinitionDtos() {
    var expectedResult = createUserProcessDefinitionDto("key");

    var queryDto = mockGetCamundaProcessDefinitions(expectedResult);
    mockFormKeys(expectedResult);

    var result = processDefinitionService.getUserProcessDefinitionDtos(queryDto);

    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(expectedResult);
  }

  private DdmProcessDefinitionDto createUserProcessDefinitionDto(String key) {
    return DdmProcessDefinitionDto.builder()
        .id("id")
        .key(key)
        .name("name")
        .suspended(false)
        .formKey("formKey")
        .build();
  }

  private void mockProcessInstanceByKey(DdmProcessDefinitionDto expectedResult) {
    var processDefinition = mockProcessDefinition(expectedResult);

    var repoService = mock(RepositoryService.class);
    when(processEngine.getRepositoryService()).thenReturn(repoService);

    var query = mock(ProcessDefinitionQuery.class);
    when(repoService.createProcessDefinitionQuery()).thenReturn(query);
    when(query.latestVersion()).thenReturn(query);
    when(query.withoutTenantId()).thenReturn(query);
    when(query.processDefinitionKey(expectedResult.getKey())).thenReturn(query);
    when(query.list()).thenReturn(List.of(processDefinition));
  }

  private ProcessDefinition mockProcessDefinition(DdmProcessDefinitionDto expectedResult) {
    var processDefinition = mock(ProcessDefinition.class);
    when(processDefinition.getId()).thenReturn(expectedResult.getId());
    when(processDefinition.getKey()).thenReturn(expectedResult.getKey());
    when(processDefinition.getName()).thenReturn(expectedResult.getName());
    when(processDefinition.isSuspended()).thenReturn(expectedResult.isSuspended());
    return processDefinition;
  }

  private void mockFormKeys(DdmProcessDefinitionDto expectedResult) {
    when(batchFormService.getStartFormKeys(Set.of(expectedResult.getId())))
        .thenReturn(Map.of(expectedResult.getId(), expectedResult.getFormKey()));
  }

  private ProcessDefinitionQueryDto mockGetCamundaProcessDefinitions(
      DdmProcessDefinitionDto expectedResult) {
    var processDefinition = mockProcessDefinition(expectedResult);

    var queryDto = mock(ProcessDefinitionQueryDto.class);
    var query = mock(ProcessDefinitionQuery.class);
    when(queryDto.toQuery(processEngine)).thenReturn(query);

    when(query.list()).thenReturn(List.of(processDefinition));
    return queryDto;
  }
}
