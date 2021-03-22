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
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ua.gov.mdtu.ddm.general.errorhandling.dto.SystemErrorDto;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ProcessDefinitionQueryDto;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.enums.SortOrder;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.ProcessDefinitionNotFoundException;

public class ProcessDefinitionRestClientIT extends BaseIT {

  @Autowired
  private ProcessDefinitionRestClient processDefinitionRestClient;

  @Test
  public void shouldReturnProcessDefinitionCount() throws JsonProcessingException {
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/process-definition/count"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(new CountResultDto(1L))))
        )
    );

    var processDefinitionsCount = processDefinitionRestClient
        .getProcessDefinitionsCount(
            ProcessDefinitionQueryDto.builder().latestVersion(true).build());

    assertThat(processDefinitionsCount.getCount()).isOne();
  }

  @Test
  public void shouldReturnListOfProcessDefinitions() throws JsonProcessingException {
    var requestDto = ProcessDefinitionQueryDto.builder().latestVersion(true)
        .sortBy(ProcessDefinitionQueryDto.SortByConstants.SORT_BY_NAME)
        .sortOrder(SortOrder.ASC.stringValue()).build();

    var processDefinitionEntity = new ProcessDefinitionEntity();
    processDefinitionEntity.setId("testId");
    var processDefinitionDto = ProcessDefinitionDto.fromProcessDefinition(processDefinitionEntity);
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/process-definition"))
            .withQueryParam("latestVersion", equalTo("true"))
            .withQueryParam("sortBy", equalTo("name"))
            .withQueryParam("sortOrder", equalTo("asc"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                    objectMapper.writeValueAsString(Lists.newArrayList(processDefinitionDto))))
        )
    );
    var processDefinitions = processDefinitionRestClient.getProcessDefinitionsByParams(requestDto);

    assertThat(processDefinitions.size()).isOne();
    assertThat(processDefinitions.get(0).getId()).isEqualTo("testId");
  }

  @Test
  public void shouldReturnOneProcessDefinition() throws JsonProcessingException {
    var processDefinitionEntity = new ProcessDefinitionEntity();
    processDefinitionEntity.setId("testId");
    var processDefinitionDto = ProcessDefinitionDto.fromProcessDefinition(processDefinitionEntity);
    restClientWireMock.addStubMapping(
        stubFor(get(urlEqualTo("/api/process-definition/testId"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(processDefinitionDto))
            ))
    );

    var processDefinition = processDefinitionRestClient.getProcessDefinition("testId");

    assertThat(processDefinition.getId()).isEqualTo("testId");
  }

  @Test
  public void shouldReturn404OnMissingProcessDefinition() throws JsonProcessingException {
    var errorDto = new SystemErrorDto("testTraceId", "type", "error", "testLocalizedMsg");
    restClientWireMock.addStubMapping(
        stubFor(get(urlEqualTo("/api/process-definition/testId404"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(404)
                .withBody(objectMapper.writeValueAsString(errorDto))
            ))
    );

    var exception = assertThrows(ProcessDefinitionNotFoundException.class,
        () -> processDefinitionRestClient.getProcessDefinition("testId404"));

    assertThat(exception.getTraceId()).isEqualTo("testTraceId");
    assertThat(exception.getCode()).isEqualTo("type");
    assertThat(exception.getMessage()).isEqualTo("error");
    assertThat(exception.getLocalizedMessage()).isEqualTo("testLocalizedMsg");
  }

  @Test
  public void shouldReturnProcessInstanceOnStartProcessDefinition() throws JsonProcessingException {
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

    var resultDto = processDefinitionRestClient
        .startProcessInstance("testId", new StartProcessInstanceDto());

    assertThat(resultDto.getId()).isEqualTo("testInstanceId");
    assertThat(resultDto.getDefinitionId()).isEqualTo("testId");
  }

  @Test
  public void shouldReturnActiveProcessDefinitions() throws JsonProcessingException {
    var requestDto = ProcessDefinitionQueryDto.builder().active(true).build();

    var processDefinitionEntity = new ProcessDefinitionEntity();
    processDefinitionEntity.setId("testId");
    var processDefinitionDto = ProcessDefinitionDto.fromProcessDefinition(processDefinitionEntity);
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/process-definition"))
            .withQueryParam("active", equalTo("true"))
            .withQueryParam("latestVersion", equalTo("false"))
            .withQueryParam("suspended", equalTo("false"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                    objectMapper.writeValueAsString(Lists.newArrayList(processDefinitionDto))))
        )
    );

    var processDefinitions = processDefinitionRestClient.getProcessDefinitionsByParams(requestDto);

    assertThat(processDefinitions.size()).isOne();
    assertThat(processDefinitions.get(0).getId()).isEqualTo("testId");
  }
}
