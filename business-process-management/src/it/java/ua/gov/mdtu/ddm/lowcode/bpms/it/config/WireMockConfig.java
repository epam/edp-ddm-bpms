package ua.gov.mdtu.ddm.lowcode.bpms.it.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.net.MalformedURLException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.gov.mdtu.ddm.lowcode.bpms.it.util.WireMockUtil;

@Configuration
public class WireMockConfig {

  @Bean(destroyMethod = "stop")
  @Qualifier("cephWireMockServer")
  public WireMockServer restClientWireMock(@Value("${ceph.http-endpoint}") String urlStr)
      throws MalformedURLException {
    return WireMockUtil.createAndStartMockServerForUrl(urlStr);
  }

  @Bean(destroyMethod = "stop")
  @Qualifier("dataFactoryMockServer")
  public WireMockServer dataFactoryWireMock(@Value("${camunda.system-variables.const.dataFactoryBaseUrl}") String urlStr)
      throws MalformedURLException {
    return WireMockUtil.createAndStartMockServerForUrl(urlStr);
  }
}
