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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.assertj.core.data.Index;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessKeyParseListenerPluginTest {

  @InjectMocks
  private BusinessKeyParseListenerPlugin businessKeyParseListenerPlugin;

  @Mock
  private ProcessEngineConfigurationImpl processEngineConfiguration;
  @Mock
  private ExpressionManager expressionManager;

  @Captor
  private ArgumentCaptor<List<BpmnParseListener>> parseListenerCaptor;

  @BeforeEach
  void setUp() {
    when(processEngineConfiguration.getExpressionManager()).thenReturn(expressionManager);
  }

  @Test
  void postInitNullListenerList() {
    when(processEngineConfiguration.getCustomPreBPMNParseListeners()).thenReturn(null);

    businessKeyParseListenerPlugin.preInit(processEngineConfiguration);

    verify(processEngineConfiguration).setCustomPreBPMNParseListeners(
        parseListenerCaptor.capture());

    var result = parseListenerCaptor.getValue();

    assertThat(result).hasSize(1)
        .element(0).isInstanceOf(BusinessKeyParseListener.class)
        .hasFieldOrProperty("businessKeyExecutionListener")
        .extracting("businessKeyExecutionListener")
        .hasFieldOrPropertyWithValue("expressionManager", expressionManager);
  }

  @Test
  void postInit() {
    var existedListener = mock(BpmnParseListener.class);
    when(processEngineConfiguration.getCustomPreBPMNParseListeners())
        .thenReturn(Lists.newArrayList(existedListener));

    businessKeyParseListenerPlugin.preInit(processEngineConfiguration);

    verify(processEngineConfiguration).setCustomPreBPMNParseListeners(
        parseListenerCaptor.capture());

    var result = parseListenerCaptor.getValue();

    assertThat(result).hasSize(2)
        .contains(existedListener, Index.atIndex(1))
        .element(0).isInstanceOf(BusinessKeyParseListener.class)
        .hasFieldOrProperty("businessKeyExecutionListener")
        .extracting("businessKeyExecutionListener")
        .hasFieldOrPropertyWithValue("expressionManager", expressionManager);
  }
}
