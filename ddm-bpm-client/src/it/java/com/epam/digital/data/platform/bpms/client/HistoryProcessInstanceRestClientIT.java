/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.bpms.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import lombok.SneakyThrows;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class HistoryProcessInstanceRestClientIT extends BaseIT {

  private static final String HISTORY_PROCESS_INSTANCE_URL = "/api/history/process-instance";
  private static final String EXTENDED_HISTORY_PROCESS_INSTANCE_URL = "/api/extended/history/process-instance";

  @Autowired
  private HistoryProcessInstanceRestClient client;

  @Test
  void shouldReturnListOfHistoryProcessInstances() {
    restClientWireMock.addStubMapping(
        stubFor(post(urlPathEqualTo(EXTENDED_HISTORY_PROCESS_INSTANCE_URL))
            .withRequestBody(equalToJson("{\"rootProcessInstances\":true,\"finished\":true,"
                + "\"unfinished\":false,\"sortBy\":\"abc\",\"sortOrder\":\"asc\"}"))
            .withQueryParam("firstResult", equalTo("2"))
            .withQueryParam("maxResults", equalTo("3"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("[{\"id\":\"id\"}]"))));

    var historyProcessInstanceQueryDto = HistoryProcessInstanceQueryDto.builder()
        .rootProcessInstances(true)
        .unfinished(false)
        .finished(true)
        .sortBy("abc")
        .sortOrder("asc")
        .build();
    var paginationQueryDto = PaginationQueryDto.builder()
        .firstResult(2)
        .maxResults(3)
        .build();
    var result = client.getHistoryProcessInstanceDtosByParams(historyProcessInstanceQueryDto,
        paginationQueryDto);

    assertThat(result).hasSize(1);
    assertThat(result.get(0))
        .hasFieldOrPropertyWithValue("id", "id");
  }

  @Test
  @SneakyThrows
  void shouldReturnHistoryListWithSetFirstAndMaxResult() {
    restClientWireMock.addStubMapping(stubFor(
        post(urlPathEqualTo(EXTENDED_HISTORY_PROCESS_INSTANCE_URL))
            .withQueryParam("firstResult", equalTo("10"))
            .withQueryParam("maxResults", equalTo("1"))
            .willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withStatus(HttpStatus.OK.value())
                .withBody("[{\"id\":\"id\",\"processDefinitionId\":\"offsetId\","
                    + "\"processDefinitionName\":\"offsetName\","
                    + "\"startTime\":\"2020-10-10T11:11:00.000Z\"}]"))));

    var processInstances = client.getHistoryProcessInstanceDtosByParams(
        HistoryProcessInstanceQueryDto.builder().build(),
        PaginationQueryDto.builder().firstResult(10).maxResults(1).build()
    );

    assertThat(processInstances).hasSize(1);
    assertThat(processInstances.get(0))
        .hasFieldOrPropertyWithValue("id", "id")
        .hasFieldOrPropertyWithValue("processDefinitionId", "offsetId")
        .hasFieldOrPropertyWithValue("processDefinitionName", "offsetName")
        .hasFieldOrPropertyWithValue("startTime", LocalDateTime.of(2020, 10, 10, 11, 11));
  }

  @Test
  void shouldReturnListOfFinishedHistoryProcessInstances() {
    restClientWireMock.addStubMapping(stubFor(
        post(urlPathEqualTo(EXTENDED_HISTORY_PROCESS_INSTANCE_URL))
            .withRequestBody(equalToJson("{\"finished\":true,\"unfinished\":false,"
                + "\"rootProcessInstances\":true,\"sortBy\":null,\"sortOrder\":null}"))
            .willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withStatus(HttpStatus.OK.value())
                .withBody("[{\"id\":\"id\",\"processDefinitionId\":\"offsetId\","
                    + "\"processDefinitionName\":\"offsetName\","
                    + "\"startTime\":\"2020-10-10T11:11:00.000Z\"}]"))));

    var processInstances = client.getHistoryProcessInstanceDtosByParams(
        HistoryProcessInstanceQueryDto.builder().finished(true).rootProcessInstances(true).build(),
        PaginationQueryDto.builder().build()
    );

    assertThat(processInstances).hasSize(1);
    assertThat(processInstances.get(0))
        .hasFieldOrPropertyWithValue("id", "id")
        .hasFieldOrPropertyWithValue("processDefinitionId", "offsetId")
        .hasFieldOrPropertyWithValue("processDefinitionName", "offsetName")
        .hasFieldOrPropertyWithValue("startTime", LocalDateTime.of(2020, 10, 10, 11, 11));
  }

  @Test
  void shouldReturnProcessInstanceById() throws JsonProcessingException {
    var historicProcessInstanceEntity = new HistoricProcessInstanceEntity();
    historicProcessInstanceEntity.setId("testId");
    var historicProcessInstanceDto = HistoricProcessInstanceDto
        .fromHistoricProcessInstance(historicProcessInstanceEntity);
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo(EXTENDED_HISTORY_PROCESS_INSTANCE_URL + "/testId"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(historicProcessInstanceDto))
            )
        )
    );

    var processInstances = client.getProcessInstanceById("testId");

    assertThat(processInstances.getId()).isEqualTo("testId");
  }

  @Test
  void shouldReturnHistoricProcessCount() throws JsonProcessingException {
    var countDto = new CountResultDto(42);
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo(HISTORY_PROCESS_INSTANCE_URL + "/count"))
            .withQueryParam("rootProcessInstances", equalTo("true"))
            .withQueryParam("finished", equalTo("true"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(countDto))
            )
        )
    );

    var processInstances = client.getProcessInstancesCount(
        HistoryProcessInstanceCountQueryDto.builder()
            .rootProcessInstances(true)
            .finished(true)
            .build()
    );

    assertThat(processInstances.getCount()).isEqualTo(42);
  }
}
