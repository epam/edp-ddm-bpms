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

package com.epam.digital.data.platform.bpms.storage;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.io.IOException;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.jupiter.api.Test;

class FileCleanerEndEventListenerIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/file_cleaner_listener.bpmn")
  void testFileCleanerEndEventListener() throws IOException {
    var processInstance = runtimeService.startProcessInstanceByKey("fileCleanerListenerKey");
    var processInstanceId = processInstance.getId();
    var taskId = taskService.createTaskQuery().taskDefinitionKey("fileCleanerListenerId")
        .singleResult().getId();

    mockConnectToKeycloak();
    digitalDocumentService.addStubMapping(
        stubFor(delete(urlEqualTo(String.format("/documents/%s", processInstanceId)))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200))));

    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  protected void mockConnectToKeycloak() throws IOException {
    keycloakMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/auth/realms/test-realm/protocol/openid-connect/token"))
            .withRequestBody(equalTo("grant_type=client_credentials"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
            )));
  }
}
