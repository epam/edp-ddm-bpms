package com.epam.digital.data.platform.bpms.extension.config;

import com.epam.digital.data.platform.bpms.extension.delegate.connector.registry.dracs.GetCertificateByBirthdateDracsRegistryDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.registry.dracs.GetCertificateByNameDracsRegistryDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.registry.edr.SearchSubjectsEdrRegistryConnectorDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.registry.edr.SubjectDetailEdrRegistryConnectorDelegate;
import com.epam.digital.data.platform.bpms.extension.exception.handler.ConnectorResponseErrorHandler;
import com.epam.digital.data.platform.starter.trembita.integration.dracs.service.DracsRemoteService;
import com.epam.digital.data.platform.starter.trembita.integration.edr.service.EdrRemoteService;
import java.util.Collection;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * The class represents a holder for beans of the general configuration. Each method produces a bean
 * and must be annotated with @Bean annotation to be managed by the Spring container. The method
 * should create, set up and return an instance of a bean.
 */
@Configuration
public class ExtensionGeneralConfig {

  @Bean
  public RestTemplate restTemplate(Collection<ClientHttpRequestInterceptor> interceptors,
      ConnectorResponseErrorHandler responseErrorHandler) {
    return new RestTemplateBuilder()
        .requestFactory(
            () -> new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
        .interceptors(interceptors)
        .errorHandler(responseErrorHandler)
        .build();
  }

  @ConditionalOnProperty(prefix = "trembita-exchange-gateway.registries.edr-registry",
      value = {"client.x-road-instance", "service.x-road-instance"})
  @Bean(name = SearchSubjectsEdrRegistryConnectorDelegate.DELEGATE_NAME)
  public SearchSubjectsEdrRegistryConnectorDelegate searchSubjectsEdrRegistryConnectorDelegate(
      EdrRemoteService edrRemoteService) {
    return new SearchSubjectsEdrRegistryConnectorDelegate(edrRemoteService);
  }

  @ConditionalOnProperty(prefix = "trembita-exchange-gateway.registries.edr-registry",
      value = {"client.x-road-instance", "service.x-road-instance"})
  @Bean(name = SubjectDetailEdrRegistryConnectorDelegate.DELEGATE_NAME)
  public SubjectDetailEdrRegistryConnectorDelegate subjectDetailEdrRegistryConnectorDelegate(
      EdrRemoteService edrRemoteService) {
    return new SubjectDetailEdrRegistryConnectorDelegate(edrRemoteService);
  }

  @Bean(name = GetCertificateByBirthdateDracsRegistryDelegate.DELEGATE_NAME)
  @ConditionalOnProperty(prefix = "trembita-exchange-gateway.registries.dracs-registry",
      value = {"client.x-road-instance", "service.x-road-instance"})
  public GetCertificateByBirthdateDracsRegistryDelegate getCertificateByBirthdateDracsRegistryDelegate(
      DracsRemoteService dracsRemoteService) {
    return new GetCertificateByBirthdateDracsRegistryDelegate(dracsRemoteService);
  }

  @Bean(name = GetCertificateByNameDracsRegistryDelegate.DELEGATE_NAME)
  @ConditionalOnProperty(prefix = "trembita-exchange-gateway.registries.dracs-registry",
      value = {"client.x-road-instance", "service.x-road-instance"})
  public GetCertificateByNameDracsRegistryDelegate getCertificateByNameDracsRegistryDelegate(
      DracsRemoteService dracsRemoteService) {
    return new GetCertificateByNameDracsRegistryDelegate(dracsRemoteService);
  }
}
