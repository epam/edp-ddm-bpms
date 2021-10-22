package com.epam.digital.data.platform.bpms.extension.it.util;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.net.MalformedURLException;
import java.net.URL;

public final class WireMockUtil {

  public static WireMockServer createAndStartMockServerForUrl(String urlStr)
      throws MalformedURLException {
    URL url = new URL(urlStr);
    WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(url.getPort()));
    WireMock.configureFor(url.getHost(), url.getPort());
    wireMockServer.start();
    return wireMockServer;
  }

  private WireMockUtil() {
  }
}
