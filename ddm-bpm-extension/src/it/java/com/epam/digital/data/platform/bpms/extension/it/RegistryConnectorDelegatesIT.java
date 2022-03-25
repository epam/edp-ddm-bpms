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
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import javax.inject.Inject;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;

public class RegistryConnectorDelegatesIT extends BaseIT {

  @Inject
  @Qualifier("platformGatewayMockServer")
  protected WireMockServer platformGatewayMockServer;

  @Test
  @Deployment(resources = "bpmn/connector/registryConnectors.bpmn")
  public void searchInAnotherRegistry() {
    platformGatewayMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo(
            "/data-factory/another_registry/test_resource"))
            .withQueryParam("searchVariable", equalTo("searchValue"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("ddm-bpm-extension"))
            .willReturn(aResponse().withStatus(200)
                .withBody("[{\"variable\":\"test_value\"}]"))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("search_in_another_registry");
    assertThat(processInstance.isEnded()).isTrue();
  }

  @Test
  @Deployment(resources = "bpmn/connector/registryConnectors.bpmn")
  public void startBpInAnotherRegistry() {
    platformGatewayMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo(
            "/bp-gateway/another_registry/api/start-bp"))
            .withRequestBody(equalToJson(
                "{\"businessProcessDefinitionKey\":\"test_business_process_key\","
                    + "\"startVariables\":{\"startVariable\":\"startValue\"}}"))
            .withHeader("Content-Type", equalTo("application/json"))
            .willReturn(aResponse().withStatus(200)
                .withBody("{\"resultVariables\":{\"variable\":\"test_value\"}}"))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("start_bp_in_another_registry");
    assertThat(processInstance.isEnded()).isTrue();
  }
}
