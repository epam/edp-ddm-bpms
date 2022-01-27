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

import com.epam.digital.data.platform.bpms.extension.service.KeycloakClientService;
import com.epam.digital.data.platform.bpms.extension.service.KeycloakClientServiceImpl;
import com.epam.digital.data.platform.integration.idm.client.KeycloakAdminClient;
import com.epam.digital.data.platform.integration.idm.dto.KeycloakClientProperties;
import com.epam.digital.data.platform.integration.idm.factory.IdmClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The class represents a holder for beans of the keycloak admin client. Each method produces a bean
 * and must be annotated with @Bean annotation to be managed by the Spring container. The method
 * should create, set up and return an instance of a bean.
 */
@Configuration
public class KeycloakClientConfig {

  @Value("${keycloak.url}")
  private String serverUrl;

  @Bean
  public IdmClientFactory idmClientFactory() {
    return new IdmClientFactory();
  }

  @Bean
  @ConditionalOnProperty(prefix = "keycloak.officer", value = "realm")
  @ConfigurationProperties(prefix = "keycloak.officer")
  public KeycloakClientProperties officerRealmProperties() {
    return new KeycloakClientProperties();
  }

  @Bean
  @ConditionalOnProperty(prefix = "keycloak.citizen", name = "realm")
  @ConfigurationProperties(prefix = "keycloak.citizen")
  public KeycloakClientProperties citizenRealmProperties() {
    return new KeycloakClientProperties();
  }

  @Bean("officer-keycloak-admin-client")
  @ConditionalOnBean(name = "officerRealmProperties")
  public KeycloakAdminClient officerKeycloakAdminClient(IdmClientFactory idmClientFactory,
      KeycloakClientProperties officerRealmProperties) {
    return idmClientFactory.keycloakAdminClient(serverUrl, officerRealmProperties);
  }

  @Bean("citizen-keycloak-admin-client")
  @ConditionalOnBean(name = "citizenRealmProperties")
  public KeycloakAdminClient citizenKeycloakAdminClient(IdmClientFactory idmClientFactory,
      KeycloakClientProperties citizenRealmProperties) {
    return idmClientFactory.keycloakAdminClient(serverUrl, citizenRealmProperties);
  }

  @Bean
  public KeycloakClientService keycloakClientService() {
    return new KeycloakClientServiceImpl();
  }
}
