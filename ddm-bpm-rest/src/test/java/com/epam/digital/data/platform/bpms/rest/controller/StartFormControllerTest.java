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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.StartFormQueryDto;
import com.epam.digital.data.platform.bpms.engine.service.BatchFormService;
import java.util.HashMap;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StartFormControllerTest {

  @InjectMocks
  private StartFormController startFormController;
  @Mock
  private BatchFormService batchFormService;

  @Test
  void getTaskProperty() {
    var startFormQueryDto = StartFormQueryDto.builder()
        .processDefinitionIdIn(Set.of("process-definition")).build();
    var expected = new HashMap<String, String>();
    when(
        batchFormService.getStartFormKeys(startFormQueryDto.getProcessDefinitionIdIn())).thenReturn(
        expected);

    var result = startFormController.getStartFormMap(startFormQueryDto);

    verify(batchFormService).getStartFormKeys(startFormQueryDto.getProcessDefinitionIdIn());
    assertThat(result).isSameAs(expected);
  }
}
