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

package com.epam.digital.data.platform.bpms.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.history.HistoricProcessInstanceRestService;
import org.camunda.bpm.engine.rest.history.HistoricTaskInstanceRestService;
import org.camunda.bpm.engine.rest.impl.TaskRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.history.HistoricProcessInstanceRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.history.HistoricTaskInstanceRestServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The class represents a holder for beans of the general configuration. Each method produces a bean
 * and must be annotated with @Bean annotation to be managed by the Spring container. The method
 * should create, set up and return an instance of a bean.
 */
@Configuration
public class GeneralRestConfig {

  @Bean
  public TaskRestService taskRestService(ObjectMapper objectMapper) {
    return new TaskRestServiceImpl(null, objectMapper);
  }

  @Bean
  public HistoricTaskInstanceRestService historicTaskInstanceRestService(ObjectMapper objectMapper,
      ProcessEngine processEngine) {
    return new HistoricTaskInstanceRestServiceImpl(objectMapper, processEngine);
  }

  @Bean
  public HistoricProcessInstanceRestService historicProcessInstanceRestService(
      ObjectMapper objectMapper, ProcessEngine processEngine) {
    return new HistoricProcessInstanceRestServiceImpl(objectMapper, processEngine);
  }
}
