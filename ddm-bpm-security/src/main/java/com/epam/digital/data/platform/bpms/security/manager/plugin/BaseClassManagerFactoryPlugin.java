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

package com.epam.digital.data.platform.bpms.security.manager.plugin;

import com.epam.digital.data.platform.bpms.security.manager.factory.BaseClassManagerFactory;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.springframework.stereotype.Component;

/**
 * {@link ProcessEnginePlugin} that is used for replacing default {@link
 * org.camunda.bpm.engine.impl.interceptor.SessionFactory session factories} in Camunda process
 * engine
 */
@Component
@RequiredArgsConstructor
public class BaseClassManagerFactoryPlugin extends AbstractProcessEnginePlugin {

  private final List<BaseClassManagerFactory<?>> genericManagerFactories;

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    var factories = new HashMap<>(processEngineConfiguration.getSessionFactories());
    genericManagerFactories.forEach(
        factory -> factories.put(factory.getSessionType(), factory));
    processEngineConfiguration.setSessionFactories(factories);
  }
}
