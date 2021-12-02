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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.bpms.api.dto.HistoryTaskCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryTaskQueryDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class HistoryTaskRestClientIT extends BaseIT {

  @Autowired
  private HistoryTaskRestClient historyTaskRestClient;

  @Test
  void shouldReturnHistoryTasks() throws JsonProcessingException {
    var task = new TaskEntity();
    task.setId("testId");
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/history/task"))
            .withQueryParam("finished", equalTo("true"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                    objectMapper.writeValueAsString(
                        Lists.newArrayList(TaskDto.fromEntity(task)))))
        )
    );

    var tasksByParams = historyTaskRestClient
        .getHistoryTasksByParams(HistoryTaskQueryDto.builder().finished(true).build());

    assertThat(tasksByParams.size()).isOne();
    assertThat(tasksByParams.get(0).getId()).isEqualTo("testId");
  }

  @Test
  void shouldReturnTaskCount() throws JsonProcessingException {
    restClientWireMock.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/history/task/count"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(new CountResultDto(1L))))
        )
    );

    var historyTaskCount = historyTaskRestClient
        .getHistoryTaskCountByParams(HistoryTaskCountQueryDto.builder().build());

    assertThat(historyTaskCount.getCount()).isOne();
  }
}