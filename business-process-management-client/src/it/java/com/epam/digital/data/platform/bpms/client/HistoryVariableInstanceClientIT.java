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
