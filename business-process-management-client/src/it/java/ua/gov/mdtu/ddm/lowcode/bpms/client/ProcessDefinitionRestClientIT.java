package ua.gov.mdtu.ddm.lowcode.bpms.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDto;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ProcessDefinitionQueryDto;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.enums.SortOrder;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.ProcessDefinitionNotFoundException;

public class ProcessDefinitionRestClientIT extends BaseIT {

  @Autowired
  private ProcessDefinitionRestClient processDefinitionRestClient;

  @Before
  public void init() throws JsonProcessingException {
    // init count response
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/process-definition/count"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(new CountResultDto(1L))))
        )
    );
    // init findOne response
    ProcessDefinitionEntity processDefinitionEntity = new ProcessDefinitionEntity();
    processDefinitionEntity.setId("testId");
    ProcessDefinitionDto processDefinitionDto = ProcessDefinitionDto
        .fromProcessDefinition(processDefinitionEntity);
    restClientWireMock.addStubMapping(
        stubFor(get(urlEqualTo("/api/process-definition/testId"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(processDefinitionDto))
            ))
    );
    // init 404 response
    ErrorDto errorDto = new ErrorDto("type", "error");
    restClientWireMock.addStubMapping(
        stubFor(get(urlEqualTo("/api/process-definition/testId404"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(404)
                .withBody(objectMapper.writeValueAsString(errorDto))
            ))
    );
    // init start process response
    var executionEntity = new ExecutionEntity();
    executionEntity.setId("testInstanceId");
    executionEntity.setProcessDefinitionId("testId");
    var processInstanceDto = ProcessInstanceDto.fromProcessInstance(executionEntity);
    restClientWireMock.addStubMapping(
        stubFor(post(urlEqualTo("/api/process-definition/testId/start"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(processInstanceDto))))
    );
  }

  @Test
  public void shouldReturnProcessDefinitionCount() {
    CountResultDto processDefinitionsCount = processDefinitionRestClient
        .getProcessDefinitionsCount(ProcessDefinitionQueryDto.builder().latestVersion(true).build());

    assertThat(processDefinitionsCount.getCount()).isOne();
  }

  @Test
  public void shouldReturnListOfProcessDefinitions() throws JsonProcessingException {
    ProcessDefinitionEntity processDefinitionEntity = new ProcessDefinitionEntity();
    processDefinitionEntity.setId("testId");
    ProcessDefinitionDto processDefinitionDto = ProcessDefinitionDto
        .fromProcessDefinition(processDefinitionEntity);
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/process-definition"))
            .withQueryParam("latestVersion", equalTo("true"))
            .withQueryParam("sortOrder", equalTo("asc"))
            .withQueryParam("active", equalTo("false"))
            .withQueryParam("sortBy", equalTo("name"))
            .withQueryParam("suspended", equalTo("false"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                    objectMapper.writeValueAsString(Lists.newArrayList(processDefinitionDto))))
        )
    );
    List<ProcessDefinitionDto> processDefinitions = processDefinitionRestClient
        .getProcessDefinitionsByParams(ProcessDefinitionQueryDto.builder().latestVersion(true)
            .sortBy(ProcessDefinitionQueryDto.SortByConstants.SORT_BY_NAME)
            .sortOrder(SortOrder.ASC.stringValue()).build());

    assertThat(processDefinitions.size()).isOne();
    assertThat(processDefinitions.get(0).getId()).isEqualTo("testId");
  }

  @Test
  public void shouldReturnOneProcessDefinition() {
    ProcessDefinitionDto processDefinition = processDefinitionRestClient
        .getProcessDefinition("testId");

    assertThat(processDefinition.getId()).isEqualTo("testId");
  }

  @Test
  public void shouldReturn404OnMissingProcessDefinition() {
    ProcessDefinitionNotFoundException exception = assertThrows(
        ProcessDefinitionNotFoundException.class,
        () -> processDefinitionRestClient.getProcessDefinition("testId404"));

    assertThat(exception.getType()).isEqualTo("type");
    assertThat(exception.getMessage()).isEqualTo("error");
  }

  @Test
  public void shouldReturnProcessInstanceOnStartProcessDefinition() {
    ProcessInstanceDto processInstanceDto = processDefinitionRestClient
        .startProcessInstance("testId", new StartProcessInstanceDto());

    assertThat(processInstanceDto.getId()).isEqualTo("testInstanceId");
    assertThat(processInstanceDto.getDefinitionId()).isEqualTo("testId");
  }

  @Test
  public void shouldReturnActiveProcessDefinitions() throws JsonProcessingException {
    ProcessDefinitionEntity processDefinitionEntity = new ProcessDefinitionEntity();
    processDefinitionEntity.setId("testId");
    ProcessDefinitionDto processDefinitionDto = ProcessDefinitionDto
        .fromProcessDefinition(processDefinitionEntity);
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/process-definition"))
            .withQueryParam("latestVersion", equalTo("false"))
            .withQueryParam("active", equalTo("true"))
            .withQueryParam("suspended", equalTo("false"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                    objectMapper.writeValueAsString(Lists.newArrayList(processDefinitionDto))))
        )
    );

    List<ProcessDefinitionDto> processDefinitions = processDefinitionRestClient
        .getProcessDefinitionsByParams(ProcessDefinitionQueryDto.builder().active(true).build());

    assertThat(processDefinitions.size()).isOne();
    assertThat(processDefinitions.get(0).getId()).isEqualTo("testId");
  }
}
