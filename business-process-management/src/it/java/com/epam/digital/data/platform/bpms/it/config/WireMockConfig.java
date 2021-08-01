package com.epam.digital.data.platform.bpms.it.config;

import com.epam.digital.data.platform.bpms.it.util.WireMockUtil;
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
  @Qualifier("trembitaMockServer")
  public WireMockServer trembitaMockServer(@Value("${trembita-exchange-gateway.url}") String urlStr)
      throws MalformedURLException {
    return WireMockUtil.createAndStartMockServerForUrl(urlStr);
  }
}
