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

package com.epam.digital.data.platform.bpms.extension.config;

import com.epam.digital.data.platform.bpms.extension.delegate.connector.DataFactoryConnectorBatchCreateDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.DataFactoryConnectorBatchReadDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.registry.dracs.GetCertificateByBirthdateDracsRegistryDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.registry.dracs.GetCertificateByNameDracsRegistryDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.registry.edr.SearchSubjectsEdrRegistryConnectorDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.registry.edr.SubjectDetailEdrRegistryConnectorDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.storage.GetContentFromCephDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.storage.PutContentToCephDelegate;
import com.epam.digital.data.platform.datafactory.excerpt.client.ExcerptFeignClient;
import com.epam.digital.data.platform.datafactory.factory.client.DataFactoryFeignClient;
import com.epam.digital.data.platform.datafactory.settings.client.UserSettingsFeignClient;
import com.epam.digital.data.platform.dso.client.DigitalSealRestClient;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.integration.ceph.service.impl.CephServiceS3Impl;
import com.epam.digital.data.platform.starter.trembita.integration.dracs.service.DracsRemoteService;
import com.epam.digital.data.platform.starter.trembita.integration.edr.service.EdrRemoteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * The class represents a holder for beans of the general configuration. Each method produces a bean
 * and must be annotated with @Bean annotation to be managed by the Spring container. The method
 * should create, set up and return an instance of a bean.
 */
@Configuration
@EnableFeignClients(
    clients = {
      DigitalSealRestClient.class,
      DataFactoryFeignClient.class,
      ExcerptFeignClient.class,
      UserSettingsFeignClient.class
    })
public class ExtensionGeneralConfig {

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

  /**
   * For delegates that use ceph service directly: {@link GetContentFromCephDelegate}, {@link
   * PutContentToCephDelegate}, {@link DataFactoryConnectorBatchCreateDelegate}, {@link
   * DataFactoryConnectorBatchReadDelegate}
   */
  @Bean
  @ConditionalOnMissingBean(CephService.class)
  public CephService cephService(
      @Value("${ceph.http-endpoint}") String cephEndpoint,
      @Value("${ceph.access-key}") String cephAccessKey,
      @Value("${ceph.secret-key}") String cephSecretKey) {
    return CephServiceS3Impl.builder()
        .cephAccessKey(cephAccessKey)
        .cephSecretKey(cephSecretKey)
        .cephEndpoint(cephEndpoint)
        .build();
  }

  @Bean
  @ConditionalOnMissingBean(RestTemplate.class)
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }
}
