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

import com.epam.digital.data.platform.bpms.engine.manager.factory.BaseClassManagerFactory;
import com.epam.digital.data.platform.bpms.engine.service.BatchFormService;
import java.util.List;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link SpringProcessEngineConfiguration} that is used
 * for overriding default camunda process engine configuration.
 */
@Component
public class LowcodeSpringProcessEngineConfiguration extends SpringProcessEngineConfiguration {

  private final BatchFormService formServiceImpl;
  private final List<BaseClassManagerFactory<?>> genericManagerFactories;

  @Autowired
  public LowcodeSpringProcessEngineConfiguration(
      BatchFormService formServiceImpl,
      BpmnParseFactory bpmnParseFactory,
      List<BaseClassManagerFactory<?>> genericManagerFactories) {
    this.formServiceImpl = formServiceImpl;
    this.bpmnParseFactory = bpmnParseFactory;
    this.genericManagerFactories = genericManagerFactories;
  }

  @Override
  public void initServices() {
    super.initServices();
    initService(formServiceImpl);
  }

  @Override
  protected void initSessionFactories() {
    super.initSessionFactories();
    genericManagerFactories.forEach(this::addSessionFactory);
  }
}
