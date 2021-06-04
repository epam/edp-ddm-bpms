package com.epam.digital.data.platform.bpms.config;

import com.epam.digital.data.platform.bpms.client.EdrRegistryClient;
import com.epam.digital.data.platform.bpms.delegate.connector.registry.SearchSubjectsEdrRegistryConnectorDelegate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaDelegatesConfig {

  @ConditionalOnProperty(prefix = "trembita-exchange-gateway",
      value = {"client.x-road-instance", "registries.edr-registry.x-road-instance"})
  @Bean(name = "searchSubjectsEdrRegistryConnectorDelegate")
  public SearchSubjectsEdrRegistryConnectorDelegate searchSubjectsEdrRegistryConnectorDelegate(
      EdrRegistryClient edrRegistryClient) {
    return new SearchSubjectsEdrRegistryConnectorDelegate(edrRegistryClient);
  }
}
