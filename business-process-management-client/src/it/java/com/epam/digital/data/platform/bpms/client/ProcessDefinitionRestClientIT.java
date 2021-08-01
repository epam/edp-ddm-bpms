package com.epam.digital.data.platform.bpms.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.epam.digital.data.platform.bpms.api.dto.ProcessDefinitionQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.SortOrder;
import com.epam.digital.data.platform.bpms.client.exception.ProcessDefinitionNotFoundException;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessInstanceWithVariablesImpl;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceWithVariablesDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.FormDto;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
  public void shouldReturnProcessInstanceWithVariablesOnStartProcessDefinitionByKey()
      throws JsonProcessingException {
    var executionEntity = new ExecutionEntity();
    executionEntity.setId("testInstanceId");
    executionEntity.setProcessDefinitionId("testId");

    var variableMap = new VariableMapImpl();
    variableMap.put("var1", "value1");

    var processInstanceWithVariables = new ProcessInstanceWithVariablesImpl(executionEntity,
        variableMap);
    var processInstanceWithVariablesDto = ProcessInstanceWithVariablesDto
        .fromProcessInstance(processInstanceWithVariables);
    restClientWireMock.addStubMapping(
        stubFor(post(urlEqualTo("/api/process-definition/key/testId/start"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(processInstanceWithVariablesDto))))
    );

    var resultDto = processDefinitionRestClient
        .startProcessInstanceByKey("testId", new StartProcessInstanceDto());

    assertThat(resultDto.getId()).isEqualTo("testInstanceId");
    assertThat(resultDto.getDefinitionId()).isEqualTo("testId");
    assertThat(resultDto.getVariables().get("var1").getValue()).isEqualTo("value1");
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

  @Test
  public void shouldReturnStartForm() throws JsonProcessingException {
    var formDto = new FormDto();
    formDto.setKey("testStartFormKey");
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/process-definition/testId/startForm"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(formDto)))
        )
    );

    var result = processDefinitionRestClient.getStartForm("testId");

    assertThat(result.getKey()).isEqualTo(formDto.getKey());
  }
}
