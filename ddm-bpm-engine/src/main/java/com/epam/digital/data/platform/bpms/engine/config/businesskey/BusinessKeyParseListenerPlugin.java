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

import com.google.common.collect.Lists;
import java.util.Objects;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.stereotype.Component;

/**
 * {@link AbstractProcessEnginePlugin} extension that is used for adding {@link
 * BusinessKeyParseListener} to {@link ProcessEngineConfigurationImpl process engine configuration}
 */
@Component
public class BusinessKeyParseListenerPlugin extends AbstractProcessEnginePlugin {

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    var expressionManager = processEngineConfiguration.getExpressionManager();
    var businessKeyExecutionListener = new BusinessKeyExecutionListener(expressionManager);
    var businessKeyParseListener = new BusinessKeyParseListener(businessKeyExecutionListener);

    registerParseListener(processEngineConfiguration, businessKeyParseListener);
  }

  private void registerParseListener(ProcessEngineConfigurationImpl processEngineConfiguration,
      BpmnParseListener parseListener) {
    final var customPreBPMNParseListeners = Lists.newArrayList(parseListener);

    if (Objects.nonNull(processEngineConfiguration.getCustomPreBPMNParseListeners())) {
      customPreBPMNParseListeners.addAll(
          processEngineConfiguration.getCustomPreBPMNParseListeners());
    }

    processEngineConfiguration.setCustomPreBPMNParseListeners(customPreBPMNParseListeners);
  }
}
