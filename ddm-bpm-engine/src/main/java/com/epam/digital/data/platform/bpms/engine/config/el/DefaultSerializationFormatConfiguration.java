/*
 * Copyright 2023 EPAM Systems.
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

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;

/**
 * {@link AbstractProcessEnginePlugin} extension that is used for setting default serialization data
 * format 'application/json' to {@link ProcessEngineConfigurationImpl process engine configuration}
 */
@Component
public class DefaultSerializationFormatConfiguration extends AbstractProcessEnginePlugin {

  private static final String DEFAULT_SERIALIZATION_FORMAT = Variables.SerializationDataFormats.JSON.getName();

  @Override
  public final void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.setDefaultSerializationFormat(DEFAULT_SERIALIZATION_FORMAT);
  }
}
