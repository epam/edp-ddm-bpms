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
import com.epam.digital.data.platform.starter.errorhandling.exception.SystemException;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class CamundaSystemErrorDelegateTest {

  @InjectMocks
  private CamundaSystemErrorDelegate delegate;
  @Mock
  private DelegateExecution delegateExecution;
  @Mock
  private NamedVariableAccessor<String> systemErrorVariableAccessor;
  @Mock
  private NamedVariableReadAccessor<String> systemErrorVariableReadAccessor;

  @Before
  public void setUp() {
    doReturn(systemErrorVariableReadAccessor).when(systemErrorVariableAccessor)
        .from(delegateExecution);

    ReflectionTestUtils.setField(delegate, "systemErrorVariable", systemErrorVariableAccessor);
  }

  @Test
  public void testEmptyCamundaSystemError() {
    when(systemErrorVariableReadAccessor.getOrDefault(StringUtils.EMPTY)).thenReturn(
        StringUtils.EMPTY);

    var exception = assertThrows(SystemException.class, () -> delegate.execute(delegateExecution));

    assertThat(exception.getMessage()).isEqualTo("System error");
    assertThat(exception.getLocalizedMessage()).isEqualTo(StringUtils.EMPTY);
  }
}
