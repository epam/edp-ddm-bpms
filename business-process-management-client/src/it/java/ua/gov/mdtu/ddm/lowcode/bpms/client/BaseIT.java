package ua.gov.mdtu.ddm.lowcode.bpms.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ua.gov.mdtu.ddm.lowcode.bpms.client.config.FeignConfig;
import ua.gov.mdtu.ddm.lowcode.bpms.it.config.WireMockConfig;

@SpringBootTest(classes = {FeignConfig.class, WireMockConfig.class})
@RunWith(SpringRunner.class)
public abstract class BaseIT {
  @Autowired
  protected WireMockServer restClientWireMock;
  @Autowired
  protected ObjectMapper objectMapper;
}
