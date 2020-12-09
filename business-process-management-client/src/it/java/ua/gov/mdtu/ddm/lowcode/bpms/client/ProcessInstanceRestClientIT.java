package ua.gov.mdtu.ddm.lowcode.bpms.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessInstanceRestClientIT extends BaseIT {

  @Autowired
  private ProcessInstanceRestClient processInstanceRestClient;

  @Before
  public void init() throws JsonProcessingException {
    // init count response
    restClientWireMock.addStubMapping(
        stubFor(get(urlEqualTo("/api/process-instance/count"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(new CountResultDto(1L))))
        )
    );
  }

  @Test
  public void shouldReturnProcessInstancesCount() {
    CountResultDto processInstancesCount = processInstanceRestClient.getProcessInstancesCount();

    assertThat(processInstancesCount.getCount()).isOne();
  }
}
