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

package com.epam.digital.data.platform.bpms.extension.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableReadAccessor;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class UserDataValidationErrorDelegateTest {

  @InjectMocks
  private UserDataValidationErrorDelegate delegate;
  @Spy
  private ObjectMapper objectMapper = new ObjectMapper();
  @Spy
  private ExecutionEntity delegateExecution = new ExecutionEntity();
  @Mock
  private NamedVariableAccessor<List<String>> validationErrorsVariableAccessor;
  @Mock
  private NamedVariableReadAccessor<List<String>> validationErrorsVariableReadAccessor;
  @Mock
  private ProcessDefinitionEntity processDefinition;

  @Before
  public void init() {
    doReturn(validationErrorsVariableReadAccessor).when(validationErrorsVariableAccessor)
        .from(delegateExecution);

    ReflectionTestUtils.setField(delegate, "validationErrorsVariable",
        validationErrorsVariableAccessor);
  }

  @Test
  public void testNoMessagesFromUser() {
    when(validationErrorsVariableReadAccessor.getOptional()).thenReturn(Optional.empty());
    when(delegateExecution.getProcessDefinition()).thenReturn(processDefinition);
    when(processDefinition.getKey()).thenReturn("testKey");

    var exception = assertThrows(ValidationException.class,
        () -> delegate.execute(delegateExecution));

    assertThat(exception).isNotNull();
    assertThat(exception.getDetails()).isNotNull();
    assertThat(exception.getDetails().getErrors()).isEmpty();
  }
}
