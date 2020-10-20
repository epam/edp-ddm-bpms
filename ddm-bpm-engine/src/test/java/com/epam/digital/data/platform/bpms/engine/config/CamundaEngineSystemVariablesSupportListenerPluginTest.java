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

package com.epam.digital.data.platform.bpms.engine.config;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CamundaEngineSystemVariablesSupportListenerPluginTest {

  @InjectMocks
  private CamundaEngineSystemVariablesSupportListenerPlugin camundaSystemVariablesSupportListenerPlugin;
  @Mock
  private CamundaEngineSystemVariablesSupportListener camundaSystemVariablesSupportListener;
  @Mock
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  @Test
  void testPreInit() {
    when(processEngineConfiguration.getCustomPreBPMNParseListeners()).thenReturn(null);

    camundaSystemVariablesSupportListenerPlugin.preInit(processEngineConfiguration);

    List<BpmnParseListener> listeners = new ArrayList<>();
    listeners.add(camundaSystemVariablesSupportListener);
    verify(processEngineConfiguration).setCustomPreBPMNParseListeners(listeners);
  }
}
