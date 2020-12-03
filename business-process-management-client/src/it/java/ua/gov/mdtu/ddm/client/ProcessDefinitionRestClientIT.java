package ua.gov.mdtu.ddm.client;

import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

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

import com.fasterxml.jackson.core.JsonProcessingException;

import mdtu.ddm.lowcode.api.dto.ProcessDefinitionQueryDto;
import mdtu.ddm.lowcode.api.dto.enums.SortOrder;

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
    // init list response
    ProcessDefinitionEntity processDefinitionEntity = new ProcessDefinitionEntity();
    processDefinitionEntity.setId("testId");
    ProcessDefinitionDto processDefinitionDto = ProcessDefinitionDto
        .fromProcessDefinition(processDefinitionEntity);
    restClientWireMock.addStubMapping(
        stubFor(get(urlEqualTo("/api/process-definition?latestVersion=true&sortOrder=asc&sortBy=name"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                    objectMapper.writeValueAsString(Lists.newArrayList(processDefinitionDto))))
        )
    );
    // init findOne response
    restClientWireMock.addStubMapping(
        stubFor(get(urlEqualTo("/api/process-definition/testId"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(processDefinitionDto))
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
    //when
    CountResultDto processDefinitionsCount = processDefinitionRestClient
        .getProcessDefinitionsCount(ProcessDefinitionQueryDto.builder().latestVersion(true).build());
    //then
    assertThat(processDefinitionsCount.getCount()).isOne();
  }

  @Test
  public void shouldReturnListOfProcessDefinitions() {
    //when
    List<ProcessDefinitionDto> processDefinitions = processDefinitionRestClient
        .getProcessDefinitionsByParams(ProcessDefinitionQueryDto.builder().latestVersion(true)
            .sortBy(ProcessDefinitionQueryDto.SortByConstants.SORT_BY_NAME)
            .sortOrder(SortOrder.ASC.stringValue()).build());
    //then
    assertThat(processDefinitions.size()).isOne();
    assertThat(processDefinitions.get(0).getId()).isEqualTo("testId");
  }

  @Test
  public void shouldReturnOneProcessDefinition() {
    //when
    ProcessDefinitionDto processDefinition = processDefinitionRestClient
        .getProcessDefinition("testId");
    //then
    assertThat(processDefinition.getId()).isEqualTo("testId");
  }

  @Test
  public void shouldReturnProcessInstanceOnStartProcessDefinition() {
    //when
    ProcessInstanceDto processInstanceDto = processDefinitionRestClient
        .startProcessInstance("testId", new StartProcessInstanceDto());
    //then
    assertThat(processInstanceDto.getId()).isEqualTo("testInstanceId");
    assertThat(processInstanceDto.getDefinitionId()).isEqualTo("testId");
  }
}
