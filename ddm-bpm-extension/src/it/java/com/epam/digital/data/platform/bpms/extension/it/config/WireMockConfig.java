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

package com.epam.digital.data.platform.bpms.extension.it.config;

import com.epam.digital.data.platform.bpms.extension.it.util.WireMockUtil;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.net.MalformedURLException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WireMockConfig {

  @Bean(destroyMethod = "stop")
  @Qualifier("dataFactoryMockServer")
  public WireMockServer dataFactoryWireMock(
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String urlStr)
      throws MalformedURLException {
    return WireMockUtil.createAndStartMockServerForUrl(urlStr);
  }

  @Bean(destroyMethod = "stop")
  @Qualifier("userSettingsWireMock")
  public WireMockServer userSettingsWireMock(
      @Value("${user-settings-service-api.url}") String urlStr)
      throws MalformedURLException {
    return WireMockUtil.createAndStartMockServerForUrl(urlStr);
  }

  @Bean(destroyMethod = "stop")
  @Qualifier("digitalSignatureMockServer")
  public WireMockServer digitalSignatureMockServer(@Value("${dso.url}") String urlStr)
      throws MalformedURLException {
    return WireMockUtil.createAndStartMockServerForUrl(urlStr);
  }

  @Bean(destroyMethod = "stop")
  @Qualifier("keycloakMockServer")
  public WireMockServer keycloakMockServer(@Value("${keycloak.url}") String urlStr)
      throws MalformedURLException {
    return WireMockUtil.createAndStartMockServerForUrl(urlStr);
  }

  @Bean(destroyMethod = "stop")
  @Qualifier("trembitaMockServerEdr")
  public WireMockServer trembitaMockServerEdr(@Value("${trembita-exchange-gateway.registries.edr-registry.trembita-url}") String urlStr)
      throws MalformedURLException {
    return WireMockUtil.createAndStartMockServerForUrl(urlStr);
  }

  @Bean(destroyMethod = "stop")
  @Qualifier("trembitaMockServerDracs")
  public WireMockServer trembitaMockServerDracs(@Value("${trembita-exchange-gateway.registries.dracs-registry.trembita-url}") String urlStr)
      throws MalformedURLException {
    return WireMockUtil.createAndStartMockServerForUrl(urlStr);
  }

  @Bean(destroyMethod = "stop")
  @Qualifier("excerptServiceWireMock")
  public WireMockServer userExcerptServiceWireMock(
      @Value("${excerpt-service-api.url}") String urlStr)
      throws MalformedURLException {
    return WireMockUtil.createAndStartMockServerForUrl(urlStr);
  }
}
