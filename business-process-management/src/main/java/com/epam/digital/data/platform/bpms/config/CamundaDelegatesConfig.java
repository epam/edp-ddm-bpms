package com.epam.digital.data.platform.bpms.config;

import com.epam.digital.data.platform.bpms.delegate.connector.registry.SearchSubjectsEdrRegistryConnectorDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.registry.SubjectDetailEdrRegistryConnectorDelegate;
import com.epam.digital.data.platform.starter.trembita.integration.service.EdrRemoteService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaDelegatesConfig {

  @ConditionalOnProperty(prefix = "trembita-exchange-gateway",
      value = {"client.x-road-instance", "registries.edr-registry.x-road-instance"})
  @Bean(name = "searchSubjectsEdrRegistryConnectorDelegate")
  public SearchSubjectsEdrRegistryConnectorDelegate searchSubjectsEdrRegistryConnectorDelegate(
      EdrRemoteService edrRemoteService) {
    return new SearchSubjectsEdrRegistryConnectorDelegate(edrRemoteService);
  }

  @ConditionalOnProperty(prefix = "trembita-exchange-gateway",
      value = {"client.x-road-instance", "registries.edr-registry.x-road-instance"})
  @Bean(name = "subjectDetailEdrRegistryConnectorDelegate")
  public SubjectDetailEdrRegistryConnectorDelegate subjectDetailEdrRegistryConnectorDelegate(
      EdrRemoteService edrRemoteService) {
    return new SubjectDetailEdrRegistryConnectorDelegate(edrRemoteService);
  }
}
