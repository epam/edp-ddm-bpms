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

package com.epam.digital.data.platform.bpms.extension.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.epam.digital.data.platform.dso.api.dto.SignResponseDto;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.LinkedHashMap;
import javax.inject.Inject;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;

public class DigitalSignatureConnectorDelegateIT extends BaseIT {

  @Inject
  @Qualifier("digitalSignatureMockServer")
  private WireMockServer digitalSignatureMockServer;

  @Test
  @Deployment(resources = {"bpmn/connector/testDigitalSignatureConnectorDelegate.bpmn"})
  public void testDigitalSignatureConnectorDelegate() throws JsonProcessingException {
    var response = SignResponseDto.builder().signature("test").build();
    digitalSignatureMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/eseal/sign"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Access-Token", equalTo(validAccessToken))
            .withRequestBody(equalTo("{\"data\":\"data to sign\"}"))
            .willReturn(
                aResponse().withHeader("Content-Type", "application/json").withStatus(200)
                    .withBody(objectMapper.writeValueAsString(response)))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("testDigitalSignatureConnectorDelegate_key");

    var cephKey = cephKeyProvider
        .generateKey("testActivity", processInstance.getProcessInstanceId());
    formDataStorageService.putFormData(cephKey, FormDataDto.builder().accessToken(validAccessToken)
        .data(new LinkedHashMap<>()).build());

    String taskId = taskService.createTaskQuery().taskDefinitionKey("waitConditionTaskForDso")
        .singleResult().getId();
    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }
}
