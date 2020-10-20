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
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import com.epam.digital.data.platform.bpms.rest.service.ProcessDefinitionService;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessDefinitionControllerTest {

  @InjectMocks
  private ProcessDefinitionController controller;
  @Mock
  private ProcessDefinitionService service;

  @Test
  void getByKey() {
    var key = "key";
    var expected = mock(DdmProcessDefinitionDto.class);

    when(service.getDdmProcessDefinitionDtoByKey(key)).thenReturn(expected);

    var result = controller.getByKey(key);

    assertThat(result).isSameAs(expected);
  }

  @Test
  void getByParams() {
    var params = mock(ProcessDefinitionQueryDto.class);
    var expected = mock(DdmProcessDefinitionDto.class);

    when(service.getDdmProcessDefinitionDtos(params)).thenReturn(List.of(expected));

    var result = controller.getByParams(params);

    assertThat(result).hasSize(1)
        .element(0).isSameAs(expected);
  }
}
