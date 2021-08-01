package com.epam.digital.data.platform.bpms.client;

import com.epam.digital.data.platform.bpms.client.config.FeignConfig;
import com.epam.digital.data.platform.bpms.it.config.WireMockConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = {FeignConfig.class, WireMockConfig.class})
@RunWith(SpringRunner.class)
public abstract class BaseIT {

  @Autowired
  protected WireMockServer restClientWireMock;
  @Autowired
  protected ObjectMapper objectMapper;
}
