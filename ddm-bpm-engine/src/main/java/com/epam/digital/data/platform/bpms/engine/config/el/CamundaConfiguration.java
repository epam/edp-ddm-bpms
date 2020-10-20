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

package com.epam.digital.data.platform.bpms.engine.config.el;

import com.epam.digital.data.platform.bpms.engine.service.SynchronizedTaskServiceImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.impl.cfg.CompositeProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.spring.boot.starter.util.CamundaSpringBootUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The class represents a holder for beans of the camunda configuration. Each method produces a bean
 * and must be annotated with @Bean annotation to be managed by the Spring container. The method
 * should create, set up and return an instance of a bean.
 */
@Configuration
@RequiredArgsConstructor
public class CamundaConfiguration {

  private final ApplicationContext appContext;
  private final LowcodeSpringProcessEngineConfiguration configuration;
  private final SynchronizedTaskServiceImpl synchronizedTaskService;

  @Bean
  public ProcessEngineConfigurationImpl processEngineConfigurationImpl(
      List<ProcessEnginePlugin> processEnginePlugins) {
    CamundaSpringBootUtil.initCustomFields(this.configuration);
    configuration.getProcessEnginePlugins()
        .add(new CompositeProcessEnginePlugin(processEnginePlugins));

    var expressionManager = new CamundaSpringExpressionManager(appContext,
        configuration.getBeans());
    configuration.setExpressionManager(expressionManager);
    configuration.setTaskService(synchronizedTaskService);
    return configuration;
  }
}
