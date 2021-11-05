package com.epam.digital.data.platform.bpms.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.history.HistoricTaskInstanceRestService;
import org.camunda.bpm.engine.rest.impl.TaskRestServiceImpl;
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
}
