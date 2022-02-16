/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.bpms.extension.config;

import com.epam.digital.data.platform.integration.idm.config.IdmClientServiceConfig;
import com.epam.digital.data.platform.integration.idm.factory.IdmServiceFactory;
import com.epam.digital.data.platform.integration.idm.model.KeycloakClientProperties;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(IdmClientServiceConfig.class)
public class IdmConfig {

  @Autowired
  public IdmServiceFactory idmServiceFactory;

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

  @Bean("officer-keycloak-client-service")
  @ConditionalOnBean(name = "officerRealmProperties")
  public IdmService officerIdmService(KeycloakClientProperties officerRealmProperties) {
    return idmServiceFactory.createIdmService(officerRealmProperties.getRealm(),
        officerRealmProperties.getClientId(),
        officerRealmProperties.getClientSecret());
  }

  @Bean("citizen-keycloak-client-service")
  @ConditionalOnBean(name = "citizenRealmProperties")
  public IdmService citizenIdmService(KeycloakClientProperties citizenRealmProperties) {
    return idmServiceFactory.createIdmService(citizenRealmProperties.getRealm(),
        citizenRealmProperties.getClientId(),
        citizenRealmProperties.getClientSecret());
  }

}
