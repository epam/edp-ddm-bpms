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

package com.epam.digital.data.platform.bpms.engine.config.businesskey;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessKeyParseListenerTest {

  @InjectMocks
  private BusinessKeyParseListener listener;
  @Mock
  private BusinessKeyExecutionListener businessKeyExecutionListener;

  @Mock
  private ActivityImpl startEventActivity;

  @Test
  void parseStartEventProcessDefinitionImpl() {
    var processDefinitionImpl = mock(ProcessDefinitionImpl.class);

    listener.parseStartEvent(null, processDefinitionImpl, startEventActivity);

    verify(startEventActivity).addBuiltInListener(ExecutionListener.EVENTNAME_START,
        businessKeyExecutionListener);
  }

  @Test
  void parseStartEventNonProcessDefinitionImpl() {
    var scopeImpl = mock(ScopeImpl.class);

    listener.parseStartEvent(null, scopeImpl, startEventActivity);

    verify(startEventActivity, never()).addBuiltInListener(any(), any());
  }
}
