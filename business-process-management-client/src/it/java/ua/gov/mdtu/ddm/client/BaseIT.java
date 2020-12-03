package ua.gov.mdtu.ddm.client;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;

import ua.gov.mdtu.ddm.bpms.it.config.FeignConfig;
import ua.gov.mdtu.ddm.bpms.it.config.WireMockConfig;

@SpringBootTest(classes = {FeignConfig.class, WireMockConfig.class})
@RunWith(SpringRunner.class)
public abstract class BaseIT {
  @Autowired
  protected WireMockServer restClientWireMock;
  @Autowired
  protected ObjectMapper objectMapper;
}
