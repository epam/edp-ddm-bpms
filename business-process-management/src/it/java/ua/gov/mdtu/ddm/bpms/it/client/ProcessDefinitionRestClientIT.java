package ua.gov.mdtu.ddm.bpms.it.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ua.gov.mdtu.ddm.bpms.it.BaseIT;
import ua.gov.mdtu.ddm.client.ProcessDefinitionRestClient;

public class ProcessDefinitionRestClientIT extends BaseIT {

  @Autowired
  private ProcessDefinitionRestClient processDefinitionRestClient;

  @Before
  public void init() throws JsonProcessingException {
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/process-definition/count"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(new CountResultDto(1L))))
        )
    );
    ProcessDefinitionEntity processDefinitionEntity = new ProcessDefinitionEntity();
    processDefinitionEntity.setId("testId");
    ProcessDefinitionDto processDefinitionDto = ProcessDefinitionDto
        .fromProcessDefinition(processDefinitionEntity);
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/process-definition"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(Lists.newArrayList(processDefinitionDto))))
        )
    );
  }

  @Test
  public void shouldReturnProcessDefinitionCount() {
    //when
    CountResultDto processDefinitionsCount = processDefinitionRestClient
        .getProcessDefinitionsCount();
    //then
    assertThat(processDefinitionsCount.getCount()).isOne();
  }

  @Test
  public void shouldReturnListOfProcessDefinitions() {
    //when
    List<ProcessDefinitionDto> processDefinitions = processDefinitionRestClient
        .getProcessDefinitions("name", "desc");
    //then
    assertThat(processDefinitions.size()).isOne();
    assertThat(processDefinitions.get(0).getId()).isEqualTo("testId");
  }
}
