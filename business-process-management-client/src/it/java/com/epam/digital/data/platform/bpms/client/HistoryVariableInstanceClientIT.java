package com.epam.digital.data.platform.bpms.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.bpms.api.dto.HistoryVariableInstanceQueryDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class HistoryVariableInstanceClientIT extends BaseIT {

  @Autowired
  private HistoryVariableInstanceClient historyVariableInstanceClient;

  @Test
  void shouldReturnVariablesInstanceListPost() throws JsonProcessingException {
    var dto = new HistoricVariableInstanceDto();
    dto.setValue("value");

    var requestDto = HistoryVariableInstanceQueryDto.builder().variableName("myVariable")
        .processInstanceId("processInstance")
        .processInstanceIdIn(Lists.newArrayList("processInstance1", "processInstance2"))
        .build();
    restClientWireMock.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/history/variable-instance"))
            .withRequestBody(equalTo(objectMapper.writeValueAsString(requestDto)))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(Lists.newArrayList(dto))))
        )
    );

    var tasksByParams = historyVariableInstanceClient.getList(requestDto);

    assertThat(tasksByParams.size()).isOne();
    assertThat(tasksByParams.get(0).getValue()).isEqualTo("value");
  }
}
