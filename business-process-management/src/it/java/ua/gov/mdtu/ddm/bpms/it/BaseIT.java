package ua.gov.mdtu.ddm.bpms.it;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ua.gov.mdtu.ddm.bpms.it.config.FeignConfig;

@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public abstract class BaseIT {

  @Autowired
  protected RuntimeService runtimeService;
  @Autowired
  protected TaskService taskService;
  @Autowired
  protected WireMockServer restClientWireMock;
  @Autowired
  protected ObjectMapper objectMapper;

}
