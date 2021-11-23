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

package com.epam.digital.data.platform.dataaccessor.completer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.VariableAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BaseCompleterVariablesReadWriteAccessorTest {

  @InjectMocks
  private BaseCompleterVariablesReadWriteAccessor baseCompleterVariablesReadWriteAccessor;
  @Mock
  private VariableAccessor variableAccessor;

  @Test
  void setTaskCompleter() {
    var taskDefinitionKey = "task";
    var completerName = "completer";

    baseCompleterVariablesReadWriteAccessor.setTaskCompleter(taskDefinitionKey, completerName);

    verify(variableAccessor).setVariable("task_completer", completerName);
  }

  @Test
  void setTaskCompleterToken() {
    var taskDefinitionKey = "task";
    var completerToken = "completerToken";

    baseCompleterVariablesReadWriteAccessor.setTaskCompleterToken(taskDefinitionKey,
        completerToken);

    verify(variableAccessor).setVariableTransient("task_completer_access_token", completerToken);
  }

  @Test
  void getTaskCompleter() {
    var taskDefinitionKey = "task";
    var completerName = "completer";
    when(variableAccessor.getVariable("task_completer")).thenReturn(completerName);

    var result = baseCompleterVariablesReadWriteAccessor.getTaskCompleter(taskDefinitionKey);

    assertThat(result).isNotEmpty().get().isSameAs(completerName);
  }

  @Test
  void getTaskCompleterToken() {
    var taskDefinitionKey = "task";
    var completerToken = "token";
    when(variableAccessor.getVariable("task_completer_access_token")).thenReturn(completerToken);

    var result = baseCompleterVariablesReadWriteAccessor.getTaskCompleterToken(taskDefinitionKey);

    assertThat(result).isNotEmpty().get().isSameAs(completerToken);
  }
}
