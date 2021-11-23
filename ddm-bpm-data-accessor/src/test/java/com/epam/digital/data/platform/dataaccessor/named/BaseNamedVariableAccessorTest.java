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

package com.epam.digital.data.platform.dataaccessor.named;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.VariableAccessor;
import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BaseNamedVariableAccessorTest {

  private static final String VARIABLE_NAME = "variable";
  private static final boolean VARIABLE_TRANSIENT_FLAG = true;

  private BaseNamedVariableAccessor<String> baseFixedVariableAccessor;

  @Mock
  private DelegateExecution delegateExecution;
  @Mock
  private VariableAccessorFactory variableAccessorFactory;
  @Mock
  private VariableAccessor variableAccessor;

  @BeforeEach
  public void setUp() {
    baseFixedVariableAccessor = new BaseNamedVariableAccessor<>(VARIABLE_NAME,
        VARIABLE_TRANSIENT_FLAG, variableAccessorFactory);

    when(variableAccessorFactory.from(delegateExecution)).thenReturn(variableAccessor);
  }

  @Test
  void from() {
    var result = baseFixedVariableAccessor.from(delegateExecution);

    assertThat(result).isInstanceOf(BaseNamedVariableReadAccessor.class);

    assertThat(ReflectionTestUtils.getField(result, "name")).isEqualTo(VARIABLE_NAME);
    assertThat(ReflectionTestUtils.getField(result, "accessor")).isEqualTo(variableAccessor);
  }

  @Test
  void on() {
    var result = baseFixedVariableAccessor.on(delegateExecution);

    assertThat(result).isInstanceOf(BaseNamedVariableWriteAccessor.class);

    assertThat(ReflectionTestUtils.getField(result, "name")).isEqualTo(VARIABLE_NAME);
    assertThat(ReflectionTestUtils.getField(result, "isTransient")).isEqualTo(
        VARIABLE_TRANSIENT_FLAG);
    assertThat(ReflectionTestUtils.getField(result, "accessor")).isEqualTo(variableAccessor);
  }
}
