/*
 * Copyright 2023 EPAM Systems.
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
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.DdmCountResultDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionAuthDto;
import com.epam.digital.data.platform.bpms.rest.service.repository.MybatisAuthorizationRepositoryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExtendedAuthorizationServiceTest {

  @InjectMocks
  private ExtendedAuthorizationService service;
  @Mock
  private MybatisAuthorizationRepositoryService repositoryService;

  @Test
  void shouldCreateProcessDefinitionAuthorizations() {
    var prDefinition = new DdmProcessDefinitionAuthDto("officer", "process-def-id");
    var listDefinitions = List.of(prDefinition);
    var expected = new DdmCountResultDto(1);

    when(repositoryService.createProcessDefinitionAuthorizations(listDefinitions)).thenReturn(1);

    var result = service.createProcessDefinitionAuthorizations(listDefinitions);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void shouldCreateProcessInstanceAuthorizations() {
    var listDefinitions = List.of("officer");
    var expected = new DdmCountResultDto(1);

    when(repositoryService.createProcessInstanceAuthorizations(listDefinitions)).thenReturn(1);

    var result = service.createProcessInstanceAuthorizations(listDefinitions);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void shouldDeleteAuthorizations() {
    var expected = new DdmCountResultDto(1);

    when(repositoryService.deleteAuthorizations()).thenReturn(1);

    var result = service.deleteAuthorizations();

    assertThat(result).isEqualTo(expected);
  }
}