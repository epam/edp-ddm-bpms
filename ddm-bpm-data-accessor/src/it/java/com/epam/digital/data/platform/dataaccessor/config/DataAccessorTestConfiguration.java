package com.epam.digital.data.platform.dataaccessor.config;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ImportAutoConfiguration(VariableAccessorAutoConfiguration.class)
public class DataAccessorTestConfiguration {

  @Bean
  public ProcessEngineConfiguration processEngineConfiguration() {
    return Mockito.mock(ProcessEngineConfiguration.class);
  }
}
