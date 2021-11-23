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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TaskPropertyRestClientIT extends BaseIT {

  @Autowired
  private TaskPropertyRestClient taskPropertyRestClient;

  @Test
  void shouldReturnTaskProperties() throws JsonProcessingException {
    Map<String, String> testTaskProperties = new HashMap<>();
    testTaskProperties.put("first", "firstValue");
    testTaskProperties.put("second", "secondValue");
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/extended/task/testId/extension-element/property"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                    objectMapper.writeValueAsString(testTaskProperties))
            )
        ));

    Map<String, String> taskProperties = taskPropertyRestClient.getTaskProperty("testId");

    assertThat(taskProperties).hasSize(2);
  }
}
