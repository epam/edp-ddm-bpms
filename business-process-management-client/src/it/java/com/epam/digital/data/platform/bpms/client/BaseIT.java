package com.epam.digital.data.platform.bpms.client;

import com.epam.digital.data.platform.bpms.client.config.FeignConfig;
import com.epam.digital.data.platform.bpms.it.config.WireMockConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(classes = {FeignConfig.class, WireMockConfig.class})
@ExtendWith(SpringExtension.class)
public abstract class BaseIT {

  @Autowired
  protected WireMockServer restClientWireMock;
  @Autowired
  protected ObjectMapper objectMapper;
}
