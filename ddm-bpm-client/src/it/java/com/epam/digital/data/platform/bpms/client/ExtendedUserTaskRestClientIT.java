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

import com.epam.digital.data.platform.bpms.api.dto.SignableUserTaskDto;
import com.epam.digital.data.platform.dso.api.dto.Subject;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ExtendedUserTaskRestClientIT extends BaseIT {

  @Autowired
  private ExtendedUserTaskRestClient extendedUserTaskRestClient;

  @Test
  void getUserTaskById() throws JsonProcessingException {
    var id = "taskId";
    var processDefinitionName = "processDefinitionName";
    var signatureValidationPack = Set.of(Subject.ENTREPRENEUR);
    var dto = new SignableUserTaskDto();
    dto.setId(id);
    dto.setProcessDefinitionName(processDefinitionName);
    dto.setSignatureValidationPack(signatureValidationPack);
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/extended/task/taskId"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(dto))))
    );

    var result = extendedUserTaskRestClient.getUserTaskById(id);

    assertThat(result)
        .hasFieldOrPropertyWithValue("id", id)
        .hasFieldOrPropertyWithValue("processDefinitionName", processDefinitionName)
        .hasFieldOrPropertyWithValue("signatureValidationPack", signatureValidationPack);
  }
}
