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

package com.epam.digital.data.platform.bpms.extension.delegate.businesskey;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableReadAccessor;
import joptsimple.internal.Strings;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DefineProcessBusinessKeyDelegateTest {

  private DefineProcessBusinessKeyDelegate delegate;
  @Mock
  private NamedVariableAccessor<String> businessKey;

  @Mock
  private DelegateExecution delegateExecution;
  @Mock
  private NamedVariableReadAccessor<String> businessKeyReadAccessor;

  @BeforeEach
  void setUp() {
    delegate = new DefineProcessBusinessKeyDelegate();
    ReflectionTestUtils.setField(delegate, "businessKey", businessKey);
    when(businessKey.from(delegateExecution)).thenReturn(businessKeyReadAccessor);
  }

  @Test
  void execute_businessKeyIsNull() throws Exception {
    when(businessKeyReadAccessor.get()).thenReturn(null);

    delegate.execute(delegateExecution);

    verify(delegateExecution).setProcessBusinessKey(null);
  }

  @Test
  void execute_businessKeyIsTooLong() throws Exception {
    when(businessKeyReadAccessor.get()).thenReturn(Strings.repeat('a', 256));

    delegate.execute(delegateExecution);

    verify(delegateExecution, never()).setProcessBusinessKey(any());
  }

  @Test
  void execute_businessKeyIsOk() throws Exception {
    when(businessKeyReadAccessor.get()).thenReturn("businessKey");

    delegate.execute(delegateExecution);

    verify(delegateExecution).setProcessBusinessKey("businessKey");
  }
}
