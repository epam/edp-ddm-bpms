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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import com.epam.digital.data.platform.bpms.rest.mapper.ProcessDefinitionMapper;
import com.epam.digital.data.platform.bpms.rest.service.repository.ProcessDefinitionRepositoryService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.ws.rs.core.Response.Status;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
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
class ProcessDefinitionServiceTest {

  @InjectMocks
  private ProcessDefinitionService processDefinitionService;
  @Mock
  private ProcessDefinitionRepositoryService processDefinitionRepositoryService;
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

    var result = processDefinitionService.getDdmProcessDefinitionDtoByKey(key);

    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void getUserProcessDefinitionDtoByKey_throwsRestException() {
    var key = "key";

    when(processDefinitionRepositoryService.getProcessDefinitionDtoByKey(key))
        .thenReturn(Optional.empty());

    var ex = assertThrows(RestException.class,
        () -> processDefinitionService.getDdmProcessDefinitionDtoByKey(key));

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

    var result = processDefinitionService.getDdmProcessDefinitionDtos(queryDto);

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

    when(processDefinitionRepositoryService.getProcessDefinitionDtoByKey(expectedResult.getKey()))
        .thenReturn(Optional.of(processDefinition));
  }

  private org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto mockProcessDefinition(
      DdmProcessDefinitionDto expectedResult) {
    var processDefinition = new ProcessDefinitionEntity();
    processDefinition.setId(expectedResult.getId());
    processDefinition.setKey(expectedResult.getKey());
    processDefinition.setName(expectedResult.getName());
    processDefinition.setSuspensionState(
        expectedResult.isSuspended() ? SuspensionState.SUSPENDED.getStateCode()
            : SuspensionState.ACTIVE.getStateCode());
    return org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto.fromProcessDefinition(processDefinition);
  }

  private void mockFormKeys(DdmProcessDefinitionDto expectedResult) {
    when(batchFormService.getStartFormKeys(Set.of(expectedResult.getId())))
        .thenReturn(Map.of(expectedResult.getId(), expectedResult.getFormKey()));
  }

  private ProcessDefinitionQueryDto mockGetCamundaProcessDefinitions(
      DdmProcessDefinitionDto expectedResult) {
    var processDefinition = mockProcessDefinition(expectedResult);

    var queryDto = mock(ProcessDefinitionQueryDto.class);
    when(processDefinitionRepositoryService.getProcessDefinitionDtos(queryDto))
        .thenReturn(List.of(processDefinition));
    return queryDto;
  }
}
