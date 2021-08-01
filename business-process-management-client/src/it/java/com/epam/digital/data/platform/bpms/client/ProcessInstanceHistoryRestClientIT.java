package com.epam.digital.data.platform.bpms.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.SortOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Collections;
import java.util.Date;
import lombok.SneakyThrows;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class ProcessInstanceHistoryRestClientIT extends BaseIT {

  private static final String HISTORY_PROCESS_INSTANCE_URL = "/api/history/process-instance";

  @Autowired
  private ProcessInstanceHistoryRestClient processInstanceHistoryRestClient;

  @Before
  public void init() throws JsonProcessingException {
    //init list response
    var historicProcessInstanceEntity = new HistoricProcessInstanceEntity();
    historicProcessInstanceEntity.setId("id");
    historicProcessInstanceEntity.setProcessDefinitionId("processDefinitionId");
    historicProcessInstanceEntity.setProcessDefinitionName("processDefinitionName");
    historicProcessInstanceEntity.setStartTime(new Date(10000L));
    var historicProcessInstanceDto = HistoricProcessInstanceDto
        .fromHistoricProcessInstance(historicProcessInstanceEntity);
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo(HISTORY_PROCESS_INSTANCE_URL))
            .withQueryParam("sortOrder", equalTo("asc"))
            .withQueryParam("unfinished", equalTo("true"))
            .withQueryParam("sortBy", equalTo("startTime"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                    objectMapper
                        .writeValueAsString(Lists.newArrayList(historicProcessInstanceDto))))
        )
    );

    // init list of finished process instance response
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo(HISTORY_PROCESS_INSTANCE_URL))
            .withQueryParam("rootProcessInstances", equalTo("true"))
            .withQueryParam("finished", equalTo("true"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                    objectMapper
                        .writeValueAsString(Lists.newArrayList(historicProcessInstanceDto))))
        )
    );
  }

  @Test
  public void shouldReturnListOfHistoryProcessInstances() {
    var processInstances = processInstanceHistoryRestClient.getProcessInstances(
        HistoryProcessInstanceQueryDto.builder().unfinished(true)
            .sortBy(HistoryProcessInstanceQueryDto.SortByConstants.SORT_BY_START_TIME)
            .sortOrder(SortOrder.ASC.stringValue()).build()
    );

    assertThat(processInstances.size()).isOne();
    assertThat(processInstances.get(0).getId()).isEqualTo("id");
    assertThat(processInstances.get(0).getProcessDefinitionId()).isEqualTo("processDefinitionId");
    assertThat(processInstances.get(0).getProcessDefinitionName())
        .isEqualTo("processDefinitionName");
    assertThat(processInstances.get(0).getStartTime()).isEqualTo(new Date(10000L));
  }

  @Test
  @SneakyThrows
  public void shouldReturnHistoryListWithSetFirstAndMaxResult() {
    var resultWithOffset = new HistoricProcessInstanceEntity();
    resultWithOffset.setId("id");
    resultWithOffset.setProcessDefinitionId("offsetId");
    resultWithOffset.setProcessDefinitionName("offsetName");
    resultWithOffset.setStartTime(new Date(420L));
    var offsetDto = HistoricProcessInstanceDto
        .fromHistoricProcessInstance(resultWithOffset);

    restClientWireMock.addStubMapping(
        stubFor(
            get(urlPathEqualTo(HISTORY_PROCESS_INSTANCE_URL))
                .withQueryParam("firstResult", equalTo("10"))
                .withQueryParam("maxResults", equalTo("1"))
                .willReturn(aResponse()
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withStatus(HttpStatus.OK.value())
                    .withBody(
                        objectMapper.writeValueAsString(
                            Collections.singletonList(offsetDto)
                        )
                    ))
        )
    );
    var processInstances = processInstanceHistoryRestClient.getProcessInstances(
        HistoryProcessInstanceQueryDto.builder()
            .firstResult(10)
            .maxResults(1)
            .build()
    );

    assertThat(processInstances.size()).isOne();
    assertThat(processInstances.get(0).getId()).isEqualTo("id");
    assertThat(processInstances.get(0).getProcessDefinitionId()).isEqualTo("offsetId");
    assertThat(processInstances.get(0).getProcessDefinitionName())
        .isEqualTo("offsetName");
    assertThat(processInstances.get(0).getStartTime()).isEqualTo(new Date(420L));
  }

  @Test
  public void shouldReturnListOfFinishedHistoryProcessInstances() {
    var processInstances = processInstanceHistoryRestClient.getProcessInstances(
        HistoryProcessInstanceQueryDto.builder().finished(true).rootProcessInstances(true).build()
    );

    assertThat(processInstances.size()).isOne();
    assertThat(processInstances.get(0).getId()).isEqualTo("id");
    assertThat(processInstances.get(0).getProcessDefinitionId()).isEqualTo("processDefinitionId");
    assertThat(processInstances.get(0).getProcessDefinitionName())
        .isEqualTo("processDefinitionName");
    assertThat(processInstances.get(0).getStartTime()).isEqualTo(new Date(10000L));
  }

  @Test
  public void shouldReturnProcessInstanceById() throws JsonProcessingException {
    var historicProcessInstanceEntity = new HistoricProcessInstanceEntity();
    historicProcessInstanceEntity.setId("testId");
    var historicProcessInstanceDto = HistoricProcessInstanceDto
        .fromHistoricProcessInstance(historicProcessInstanceEntity);
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/history/process-instance/testId"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(historicProcessInstanceDto))
            )
        )
    );

    var processInstances = processInstanceHistoryRestClient.getProcessInstanceById("testId");

    assertThat(processInstances.getId()).isEqualTo("testId");
  }

  @Test
  public void shouldReturnHistoricProcessCount() throws JsonProcessingException {
    var countDto = new CountResultDto(42);
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/history/process-instance/count"))
            .withQueryParam("rootProcessInstances", equalTo("true"))
            .withQueryParam("finished", equalTo("true"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(countDto))
            )
        )
    );

    var processInstances = processInstanceHistoryRestClient.getProcessInstancesCount(
        HistoryProcessInstanceCountQueryDto.builder()
        .rootProcessInstances(true)
        .finished(true)
        .build()
    );

    assertThat(processInstances.getCount()).isEqualTo(42);
  }
}
