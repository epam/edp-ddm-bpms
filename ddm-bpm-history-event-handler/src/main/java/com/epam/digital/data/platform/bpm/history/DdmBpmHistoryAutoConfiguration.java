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

package com.epam.digital.data.platform.bpm.history;

import com.epam.digital.data.platform.bpm.history.base.handler.LevelBasedHistoryEventHandlerWrapper;
import com.epam.digital.data.platform.bpm.history.base.level.TypeBasedHistoryLevelEnum;
import com.epam.digital.data.platform.bpm.history.base.plugin.ProcessHistoryEventHandlerPlugin;
import com.epam.digital.data.platform.bpms.rest.service.repository.ProcessInstanceRuntimeService;
import java.util.List;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.history.handler.CompositeHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.DbHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Autoconfiguration class that creates a composite history event handler (with default history
 * event handler and scanned custom {@link HistoryEventHandler HistoryEventHandlers}) and register
 * it in Camunda context using {@link ProcessHistoryEventHandlerPlugin}
 */
@Configuration
@ComponentScan
public class DdmBpmHistoryAutoConfiguration {

  @Bean
  public ProcessHistoryEventHandlerPlugin processHistoryEventHandlerPlugin(
      HistoryEventHandler handler) {
    return new ProcessHistoryEventHandlerPlugin(handler);
  }

  @Bean
  @Primary
  public HistoryEventHandler historyEventHandler(List<HistoryEventHandler> historyEventHandlers) {
    return new CompositeHistoryEventHandler(historyEventHandlers);
  }

  @Bean
  public LevelBasedHistoryEventHandlerWrapper levelBasedHistoryEventHandlerWrapper(
      @Value("${camunda.bpm.database-history-level:FULL}") String historyLevel) {
    var level = TypeBasedHistoryLevelEnum.fromName(historyLevel);

    return new LevelBasedHistoryEventHandlerWrapper(level, new DbHistoryEventHandler());
  }

  @Bean
  @ConditionalOnMissingBean(ProcessInstanceRuntimeService.class)
  public ProcessInstanceRuntimeService processInstanceHistoricService(
      RuntimeService runtimeService, ProcessEngine processEngine) {
    return new ProcessInstanceRuntimeService(runtimeService, processEngine);
  }
}
