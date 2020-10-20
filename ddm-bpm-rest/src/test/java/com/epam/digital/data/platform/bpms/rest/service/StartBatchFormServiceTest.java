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
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.rest.cmd.GetStartFormKeysCmd;
import java.util.Map;
import java.util.Set;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StartBatchFormServiceTest {

  @InjectMocks
  private BatchFormService batchFormService;
  @Mock
  private CommandExecutor commandExecutor;

  @Test
  void shouldReturnEmptyMapAsTaskHasEmptyProperty() {
    var existedProcessDefinition = "existed";
    var nonExistedProcessDefinition = "notExisted";

    when(commandExecutor.execute(new GetStartFormKeysCmd(
        Set.of(existedProcessDefinition, nonExistedProcessDefinition)))).thenReturn(
        Map.of(existedProcessDefinition, "form-key"));

    var formKeys = batchFormService.getStartFormKeys(
        Set.of(existedProcessDefinition, nonExistedProcessDefinition));

    assertThat(formKeys).containsEntry(existedProcessDefinition, "form-key");
  }
}
