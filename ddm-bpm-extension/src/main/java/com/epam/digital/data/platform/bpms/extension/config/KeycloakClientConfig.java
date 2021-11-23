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
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
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
  @Value("${keycloak.citizen.realm}")
  private String citizenRealm;
  @Value("${keycloak.citizen.client-id}")
  private String citizenClientId;
  @Value("${keycloak.citizen.client-secret}")
  private String citizenClientSecret;
  @Value("${keycloak.officer.realm}")
  private String officerRealm;
  @Value("${keycloak.officer.client-id}")
  private String officerClientId;
  @Value("${keycloak.officer.client-secret}")
  private String officerClientSecret;


  @Bean("citizen-keycloak-client")
  public Keycloak citizenKeycloak() {
    return KeycloakBuilder.builder()
        .serverUrl(serverUrl)
        .realm(citizenRealm)
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId(citizenClientId)
        .clientSecret(citizenClientSecret)
        .build();
  }

  @Bean("officer-keycloak-client")
  public Keycloak officerKeycloak() {
    return KeycloakBuilder.builder()
        .serverUrl(serverUrl)
        .realm(officerRealm)
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId(officerClientId)
        .clientSecret(officerClientSecret)
        .build();
  }

  @Bean("citizen-keycloak-service")
  public KeycloakClientService citizenKeycloakService() {
    return new KeycloakClientServiceImpl(citizenRealm, citizenKeycloak());
  }

  @Bean("officer-keycloak-service")
  public KeycloakClientService officerKeycloakService() {
    return new KeycloakClientServiceImpl(officerRealm, officerKeycloak());
  }
}
